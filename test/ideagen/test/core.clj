(ns ideagen.test.core
  (:use ideagen.core
        [clojure.data.xml]
        clojure.test))


(deftest create-simple
  (is (=
    (parse (java.io.FileReader. "test/resources/iml/simple.iml"))
    (do
      (emit-module
        (-> (create-module {:test ["test"]})
            (with-library
              {:name "JUnit4"
               :scope :test
               :classes [{:param :APPLICATION_HOME_DIR :jar "lib/junit-4.11.jar"}
                         {:param :APPLICATION_HOME_DIR :jar "lib/hamcrest-core-1.3.jar"}
                         {:param :APPLICATION_HOME_DIR :jar "lib/hamcrest-library-1.3.jar"}]}))
        "test/resources/iml/simple-gen.iml")
      (parse (java.io.FileReader. "test/resources/iml/simple-gen.iml"))))))


(deftest create-complex-paramed-with-module-and-project-deps
  (is (=
    (parse (java.io.FileReader. "test/resources/iml/paramed-module-project-deps.iml"))
    (do
      (emit-module
        (-> (create-module)
            (with-src)
            (with-library {:classes ["lib/jstl-api-1.2.jar"]})
            (with-library {:classes [{:param :SQL_DB_LIB :jar "hsqldb.jar"}]})
            (with-library {:ref {:name "jstl-impl-1.2"}}))
        "test/resources/iml/paramed-module-project-deps-gen.iml")
      (parse (java.io.FileReader. "test/resources/iml/paramed-module-project-deps-gen.iml"))))))
