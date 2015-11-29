(ns ideagen.test.eclipse
  (:use ideagen.core
        ideagen.eclipse
        [clojure.data.xml]
        [clojure.test :exclude [with-test]]))

(deftest iml-from-simple-eclipse-classpath
  (is (=
    (parse (java.io.FileReader. "test/resources/iml/ecl-with-param-lib-extlib.iml"))
    (do
      (emit-module (eclipse-to-iml "test/resources/eclipse/ecl-with-param-lib-extlib.classpath")
        "test/resources/iml/ecl-with-param-lib-extlib.gen.iml")
      (parse (java.io.FileReader. "test/resources/iml/ecl-with-param-lib-extlib.gen.iml"))))))


;; TEST project dependency

;; TEST -x exclude option for .classpath import
