(ns puertorico.prboot
  (:require [boot.core :as bootcore]
            [puertorico.server :as server]))

(bootcore/deftask initialize-dev []
                  (bootcore/with-pass-thru _
                                           (server/-main)))
