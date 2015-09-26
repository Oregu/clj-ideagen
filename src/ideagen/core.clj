(ns ideagen.core
  (:use [clojure.data.xml]))

(defn create-module []
  {:version 4
   :content   []
   :structure []})

(defn- with-content [module type dir]
  (update-in module [:content] conj {type dir}))

(defn with-src
  ([module]     (with-src module "src"))
  ([module dir] (with-content module :src dir)))

(defn with-test
  ([module]     (with-test module "test"))
  ([module dir] (with-content module :test dir)))

(defn with-res
  ([module]     (with-res module "resources"))
  ([module dir] (with-content module :res dir)))

(defn with-test-res
  ([module]     (with-test-res module "test-resources"))
  ([module dir] (with-content module :test-res dir)))

(defn excluding
  ([module]     (excluding module "out"))
  ([module dir] (with-content module :excl dir)))

(defn with-lib [module lib]
  (update-in module [:structure] conj lib))

(defn with-jdk [module]
  (update-in module [:structure] conj :jdk))

(defn- lib-scope [lib]
  (when
    (and (:scope lib) (not= (:scope lib) :compile))
    {:scope (.toUpperCase (name (:scope lib)))}))

(defn- lib-reference [lib]
  (let [lib-ref (:ref lib)]
    (when lib-ref
      {:type "library"
       :name (:name lib-ref)
       :level "project"})))

(defn- path-param [p]
  (if (keyword? p)
    (str "$" (name p) "$")
    p))

;; $MODULE_DIR$ is default parent dir for dependencies and project folders.
;; jar:// protocol for libraries by default.

(defn- construct-path [lib]
  (let [dir (if (:param lib) (path-param (:param lib)) "$MODULE_DIR$")
        jar (if (string? lib) lib (:jar lib))]
    (if (.startsWith jar "/")
      (str "jar://" lib "!/")
      (if (.endsWith jar ".jar")
        (str "jar://" dir "/" jar "!/")
        (if (empty? jar)
          (str "file://" dir)
          (str "file://" dir "/" jar))))))

(defn- to-library-entry [lib]
  (element :orderEntry
    (merge {:type "module-library"} (lib-scope lib) (lib-reference lib))
    (when-not (:ref lib)
      (element :library (when (:name lib) {:name (:name lib)})
        (element :CLASSES {}
          (map #(element :root {:url (construct-path %)})
            (:classes lib)))
        (element :JAVADOC)
        (element :SOURCES)))))

(defn- to-content-type [k]
  (condp = k
    :src  {:isTestSource false}
    :test {:isTestSource true}
    :res  {:type "java-resource"}
    :test-res {:type "java-test-resource"}
    :excl nil))

(defn- to-content-dir [entry]
  (let [entry (first entry)]
    (element :sourceFolder
      (merge
        {:url (str "file://$MODULE_DIR$/" (val entry))}
        (to-content-type (key entry))))))

;; 'compile' scope is default scope.
;; 'project' level library reference is default level.
;; default module reference type is library.

(defn- to-element [module]
  (element :module {:type "JAVA_MODULE" :version (:version module)}
    (element :component {:name "NewModuleRootManager" :inherit-compiler-output true}
      (element :exclude-output)
      (element :content {:url "file://$MODULE_DIR$"}
        (if (empty? (:content module))
          (to-content-dir {:src "src"})
          (map to-content-dir (:content module))))
      (element :orderEntry {:type "inheritedJdk"})
      (element :orderEntry {:type "sourceFolder" :forTests "false"})
      (map to-library-entry (:structure module)))))

(defn emit-module [module filepath]
  (with-open [out-file (java.io.FileWriter. filepath)]
    (indent (to-element module) out-file)))
