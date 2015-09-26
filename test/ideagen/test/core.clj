(ns ideagen.test.core
  (:use ideagen.core
        [clojure.data.xml]
        [clojure.test :exclude [with-test]]))

(deftest simple-module
  (is (=
    (parse (java.io.FileReader. "test/resources/iml/simple.iml"))
    (do
      (emit-module
        (-> (create-module)
            (with-jdk)
            (with-src)
            (with-test)
            (with-lib
              {:name "JUnit4"
               :scope :test
               :classes [{:param :APPLICATION_HOME_DIR :jar "lib/junit-4.11.jar"}
                         {:param :APPLICATION_HOME_DIR :jar "lib/hamcrest-core-1.3.jar"}
                         {:param :APPLICATION_HOME_DIR :jar "lib/hamcrest-library-1.3.jar"}]}))
        "test/resources/iml/simple.gen.iml")
      (parse (java.io.FileReader. "test/resources/iml/simple.gen.iml"))))))


(deftest complex-paramed-with-module-and-project-deps
  (is (=
    (parse (java.io.FileReader. "test/resources/iml/paramed-module-project-deps.iml"))
    (do
      (emit-module
        (-> (create-module)
            (with-jdk)
            (with-src)
            (with-lib {:classes ["lib/jstl-api-1.2.jar"]})
            (with-lib {:classes [{:param :SQL_DB_LIB :jar "hsqldb.jar"}]})
            (with-lib {:ref {:name "jstl-impl-1.2"}}))
        "test/resources/iml/paramed-module-project-deps.gen.iml")
      (parse (java.io.FileReader. "test/resources/iml/paramed-module-project-deps.gen.iml"))))))


(deftest many-sources-different-deps-order
  (is (=
    (parse (java.io.FileReader. "test/resources/iml/more-sources-diff-order.iml"))
    (do
      (emit-module
        (-> (create-module)
            (with-lib {:classes ["lib/jstl-api-1.2.jar"]})
            (with-src "main/java/gen-src")
            (with-src "main/java/src")
            (with-res "main/java/resources")
            (with-test "main/test/src")
            (with-test-res "main/test/resources")
            (excluding "target")
            (with-lib {:classes [{:jar "lib/classes"}]})
            (with-jdk)
            (with-lib {:classes [{:jar "classes"}]}))
        "test/resources/iml/more-sources-diff-order.gen.iml")
      (parse (java.io.FileReader. "test/resources/iml/more-sources-diff-order.gen.iml"))))))
