(ns ideagen.test.core
  (:use ideagen.core
        [clojure.data.xml]
        clojure.test))

(deftest create-simple
  (is (=
    (parse (java.io.FileReader. "test/iml/simple.iml"))
    (do
      (emit-module "test/iml/simple-gen.iml"
        (-> (create-module)
            (with-library
              {:name "JUnit4"
               :classes ["lib/junit-4.11.jar"
                         "lib/hamcrest-core-1.3.jar"
                         "lib/hamcrest-library-1.3.jar"]})))
      (parse (java.io.FileReader. "test/iml/simple-gen.iml"))))))
