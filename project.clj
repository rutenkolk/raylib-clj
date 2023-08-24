(defproject raylib-clj "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [cnuernber/dtype-next "10.002"]
                 ;[net.java.dev.jna/jna "5.13.0"]
                 [org.suskalo/coffi "0.6.409"]
                 [camel-snake-kebab "0.4.3"]
                 ]
  :main ^:skip-aot raylib-clj.core
  :target-path "target/%s"
  :jvm-opts ["-Dclojure.compiler.direct-linking=true"
             "--enable-preview"
             ;"--add-modules=jdk.incubator.foreign"
             "--enable-native-access=ALL-UNNAMED"
             "-Dforeign.restricted=permit"
             "--add-opens" "java.base/java.lang=ALL-UNNAMED"
             ]
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"
                                  "--enable-preview"
                                  ;"--add-modules=jdk.incubator.foreign"
                                  "--enable-native-access=ALL-UNNAMED"
                                  "-Dforeign.restricted=permit"
                                  "--add-opens" "java.base/java.lang=ALL-UNNAMED"
                                  ]}})




