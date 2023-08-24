(defproject raylib-clj "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [cnuernber/dtype-next "10.002"]
                 [net.java.dev.jna/jna "5.13.0"]
                 ]
  :main ^:skip-aot raylib-clj.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"
                                  "--add-modules" "jdk.incubator.foreign" "-Dforeign.restricted=permit" "--add-opens" "java.base/java.lang=ALL-UNNAMED"
                                  "--enable-preview"
                                  ]}})




