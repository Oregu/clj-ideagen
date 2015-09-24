(ns ideagen.core
  (:use [clojure.data.xml]))

(defn create-module
  ([] (create-module nil))
  ([module]
    (merge {:version 4
            :exclude-output nil
            :src []
            :deps []}
      module)))

(defn with-src
  ([module]
    (with-src module "src"))
  ([module dir]
    (update-in module [:src] conj dir)))

(defn with-library [module lib]
  (update-in module [:deps] conj lib))

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
        lib (if (string? lib) lib (:jar lib))]
  (str "jar://" dir "/" lib "!/")))

(defn- library-entry [lib]
  (element :orderEntry
    (merge {:type "module-library"} (lib-scope lib) (lib-reference lib))
    (when-not (:ref lib)
      (sexp-element :library (when (:name lib) {:name (:name lib)})
        [[:CLASSES {}
          (map (fn [lib] [:root {:url (construct-path lib)}])
            (:classes lib))]
         [:JAVADOC]
         [:SOURCES]]))))

(defn- source-dir
  ([dir]
    (source-dir dir false))
  ([dir test?]
    (element :sourceFolder {:url (str "file://$MODULE_DIR$/" dir) :isTestSource test?})))

;; 'compile' scope is default scope.
;; 'project' level library reference is default level.
;; default module reference type is library.

(defn- to-element [module]
  (element :module {:type "JAVA_MODULE" :version (:version module)}
    (element :component {:name "NewModuleRootManager" :inherit-compiler-output true}
      (element :exclude-output) ;; ADD EXCLUDE OUTPUT HERE
      (element :content {:url "file://$MODULE_DIR$"}
        (if (empty? (:src module))
          (source-dir "src")
          (map source-dir (:src module)))
        (map #(source-dir % true) (:test module)))
      (element :orderEntry {:type "inheritedJdk"})
      (element :orderEntry {:type "sourceFolder" :forTests "false"})
      (map library-entry (:deps module)))))

(defn emit-module [module filepath]
  (with-open [out-file (java.io.FileWriter. filepath)]
    (indent (to-element module) out-file)))
