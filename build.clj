(ns build
  "tasks for building artifacts"
  (:require
   [clojure.java.io :as io]
   [clojure.tools.build.api :as b]
   [clojure.string :as s]
   [cheshire.core :as json]
   [clj-http.client :as client]
   ))

(def lib-coord 'wiredaemon/raylib-clj)
(def version (format "0.1.%s" (b/git-count-revs nil)))

(def resource-dirs ["resources/"])
(def source-dirs ["src/"])

(def target-dir "target/")
(def class-dir (str target-dir "classes/"))

(def basis (b/create-basis {:project "deps.edn"}))

(def jar-file (str target-dir "raylib-clj.jar"))

(defn clean [opts]
  (b/delete {:path target-dir})
  opts)

(defn- exists? "Checks if file exists" [& paths]
  (.exists ^java.io.File (apply io/file paths)))

(defn- write-pom "Writes a pom file" [opts]
  (b/write-pom {:basis basis
                :class-dir class-dir
                :lib lib-coord
                :version version
                :scm {:url "https://github.com/rutenkolk/raylib-clj"
                      :connection "scm:git:git://github.com/rutenkolk/raylib-clj.git"
                      :developerConnection "scm:git:ssh://git@github.com/rutenkolk/raylib-clj.git"
                      :tag (str "v" version)}
                :src-dirs source-dirs})
  (b/copy-file {:src (b/pom-path {:lib lib-coord
                                  :class-dir class-dir})
                :target (str target-dir "pom.xml")})
  opts)


(defn pom
  "Generates a `pom.xml` file in the `target/classes/META-INF` directory.
  If `:pom/output-path` is specified, copies the resulting pom file to it."
  [opts]
  (write-pom opts)
  (when-some [path (:output-path opts)]
    (b/copy-file {:src (b/pom-path {:lib lib-coord
                                    :class-dir class-dir})
                  :target path}))
  opts)

(defn- copy-resources
  "Copies the resources from the [[resource-dirs]] to the [[class-dir]]."
  [opts]
  (b/copy-dir {:target-dir class-dir
               :src-dirs resource-dirs})
  opts)

(defn jar
  "Generates a `raylib-clj.jar` file in the `target/` directory"
  [opts]
  (write-pom opts)
  (copy-resources opts)
  (when-not (exists? target-dir jar-file)
    (b/copy-dir {:target-dir class-dir
                 :src-dirs source-dirs})
    (b/jar {:class-dir class-dir
            :jar-file jar-file}))
  opts)

(defn run-tasks
  "Runs a series of tasks with a set of options.
  The `:tasks` key is a list of symbols of other task names to call. The rest of
  the option keys are passed unmodified."
  [opts]
  (println "opts are:")
  (clojure.pprint/pprint opts)
  (binding [*ns* (find-ns 'build)]
    (reduce
     (fn [opts task]
       ((resolve task) opts))
     opts
     (:tasks opts))))

