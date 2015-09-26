(ns ideagen.eclipse
  (:use clojure.data.xml
        ideagen.core)
  (:gen-class))

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
    (keyword
      (if (< ind 0)
        path
        (.substring path 0 ind)))))

(defn get-classpath-jar [path param]
  (let [jar (.substring path (.length (name param)))]
    (if (empty? jar)
      ""
      (.substring jar 1))))

(defn lib-from-var [cp-entry]
  (let [path (:path cp-entry)
        param-val (get-classpath-param path)
        jar-val   (get-classpath-jar path param-val)]
    {:param param-val
     :jar jar-val}))

(defn with-classpath-seq [module cp-seq]
  (reduce
    (fn [module cp-entry]
      (condp identical? (:kind cp-entry)
        :src (with-src module (:path cp-entry)) ;; Check if contains test or resources, add appropriately
        :con (with-jdk module)
        :lib (with-lib module {:classes [(:path cp-entry)]})
        :var (with-lib module {:classes [(lib-from-var cp-entry)]})
        module)) ;; nothing by default
    module
    cp-seq))

(defn eclipse-to-iml [ecl-file]
  (let [cp-seq (classpath-seq (read-classpath ecl-file))]
    (with-classpath-seq (create-module) cp-seq)))

(defn -main [ecl iml]
  (emit-module (eclipse-to-iml ecl) iml))
