(ns ideagen.project
  (:use clojure.data.xml
        ideagen.core)
  (:import java.io.File)
  (:gen-class))

(defn- relative-path [file1 file2]
  (let [name1 (.getCanonicalPath file1)
        name2 (.getCanonicalPath file2)]
    (if (.startsWith name1 name2)
      (.substring name1 (inc (count name2))) ;; inc for leading file separator
      name1)))

(defn scan-modules [dir deep]
  (let [dir (File. dir)
        modules (filter #(.endsWith (.getName %) ".iml") (file-seq dir))
        relatives (map #(relative-path % dir) modules)
        sorted (sort relatives)]
    {:version 4
     :modules sorted}))

(defn gen-project
  ([dirFrom] (gen-project dirFrom "." 0 7))
  ([dirFrom out] (gen-project dirFrom out 0 7))
  ([dirFrom out deep] (gen-project dirFrom out deep 7))
  ([dirFrom out deep jdk]
    (-> (scan-modules dirFrom deep)
        (merge {:jdk jdk})
        (emit-idea out))))

(defn -main [dirFrom & opts]
  (let [opts (apply hash-map opts)
        out  (if (contains? opts "-o") (get opts "-o") ".")
        jdk  (if (contains? opts "-jdk") (Integer. (get opts "-jdk")) 8)
        deep (when (contains? opts "-d") (Integer. (get opts "-d")))]
    (gen-project dirFrom out deep jdk)))
