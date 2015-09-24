(ns ideagen.eclipse
  (:use [clojure.data.xml]
        [ideagen.core]))

(defn read-classpath [file]
  (parse (java.io.FileReader. file)))

(defn- extract-entry [cp-entry]
  (let [attrs (:attrs cp-entry)]
    {:kind (keyword (:kind attrs))
     :path (:path attrs)}))

(defn classpath-seq [cp-el]
  (map extract-entry (:content cp-el)))

(defn convert-to-iml [ecl-file]
  (let [cp-seq (classpath-seq (read-classpath ecl-file))]
    (-> (create-module)
        ())))
