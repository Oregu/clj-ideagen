(ns ideagen.core
  (:use clojure.data.xml))

(defn create-module []
  {:version 4
   :content   []
   :structure []})

(defn- with-content [module type dir]
  (update-in module [:content] conj {type dir}))

(defn with-lib [module lib]
  (update-in module [:structure] conj lib))

(defn with-jdk [module]
  (update-in module [:structure] conj :jdk))

(defn with-module-dep [module dep-module]
  (update-in module [:structure] conj {:module dep-module}))

(defn with-src
  ([module]     (with-src module "src"))
  ([module dir]
    (let [module (with-content module :src dir)]
      (if (not (pos? (.indexOf (:structure module) :src)))
        (update-in module [:structure] conj :src)
        module))))

(defn with-res
  ([module]     (with-res module "resources"))
  ([module dir] (with-content module :res dir)))

(defn with-test
  ([module]     (with-test module "test"))
  ([module dir] (with-content module :test dir)))

(defn with-test-res
  ([module]     (with-test-res module "test-resources"))
  ([module dir] (with-content module :test-res dir)))

(defn excluding
  ([module]     (excluding module "out"))
  ([module dir] (with-content module :excl dir)))

(defn- lib-scope [lib]
  (when
    (and (:scope lib) (not (identical? (:scope lib) :compile)))
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

(defn- to-order-entry [lib]
  (condp identical? lib
    :jdk (element :orderEntry {:type "inheritedJdk"})
    :src (element :orderEntry {:type "sourceFolder" :forTests false})
    (if (:module lib)
      (element :orderEntry {:type "module" :module-name (:module lib)})
      (element :orderEntry
        (merge {:type "module-library"} (lib-scope lib) (lib-reference lib))
        (when-not (:ref lib)
        (element :library (when (:name lib) {:name (:name lib)})
          (element :CLASSES {}
            (map #(element :root {:url (construct-path %)})
              (:classes lib)))
          (element :JAVADOC)
          (element :SOURCES)))))))

(defn- to-content-type [k]
  (condp identical? k
    :src  {:isTestSource false}
    :test {:isTestSource true}
    :res  {:type "java-resource"}
    :test-res {:type "java-test-resource"}
    nil))

(defn- to-content-dir [entry]
  (let [entry (first entry)]
    (element (if (identical? (key entry) :excl) :excludeFolder :sourceFolder)
      (merge
        {:url (str "file://$MODULE_DIR$/" (val entry))}
        (to-content-type (key entry))))))

(defn- to-proj-module [entry]
  (element :module
    {:fileurl (str "file://$PROJECT_DIR$/" entry)
     :filepath (str "$PROJECT_DIR$/" entry)}))

;; 'compile' scope is default scope.
;; 'project' level library reference is default level.
;; default module reference type is library.

(defn- to-iml [module]
  (element :module {:type "JAVA_MODULE" :version (:version module)}
    (element :component {:name "NewModuleRootManager" :inherit-compiler-output true}
      (element :exclude-output)
      (element :content {:url "file://$MODULE_DIR$"}
        (if (empty? (:content module))
          (to-content-dir {:src "src"})
          (map to-content-dir (:content module))))
      (map to-order-entry (:structure module)))))

(defn- to-modules [proj]
  (element :project {:version (:version proj)}
    (element :component {:name "ProjectModuleManager"}
      (element :modules nil
        (map to-proj-module (:modules proj))))))

(defn- to-misc [proj]
  (element :project {:version (:version proj)}
    (element :component
      {:name "ProjectRootManager"
       :version 2
       :languageLevel (str "JDK_1_" (:jdk proj))
       :default false
       "assert-keyword" true
       "project-jdk-type" "JavaSDK"
       "project-jdk-name" (str "1." (:jdk proj))})))

(defn emit-module [module filepath]
  (with-open [out-file (java.io.FileWriter. filepath)]
    (indent (to-iml module) out-file)))

(defn emit-idea [proj filepath]
  (let [ideaDir (java.io.File. filepath ".idea")]
    (.mkdirs ideaDir)
    (with-open [out-file (java.io.FileWriter.
                            (java.io.File. ideaDir "modules.xml"))]
      (indent (to-modules proj) out-file))
    (with-open [out-file (java.io.FileWriter.
                            (java.io.File. ideaDir "misc.xml"))]
      (indent (to-misc proj) out-file))))
