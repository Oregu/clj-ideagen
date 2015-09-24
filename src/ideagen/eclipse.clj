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

(defn get-classpath-param [path]
  (let [ind (.indexOf path "/")]
    (if (< ind 0)
      path
      (.substring path 0 ind))))

(defn with-classpath-seq [module cp-seq]
  (reduce
    (fn [module cp-entry]
      (condp = (:kind cp-entry)
        :src (with-src module (:path cp-entry))
        :lib (with-lib module {:classes [(:path cp-entry)]})
        :var (with-lib module {:classes [{:param (get-classpath-param (:path cp-entry))
                                          :jar (:path cp-entry)}]})
        module)) ;; nothing by default
    cp-seq))

(defn eclipse-to-iml [ecl-file]
  (let [cp-seq (classpath-seq (read-classpath ecl-file))]
    (-> (create-module)
        (with-classpath-seq cp-seq))))
