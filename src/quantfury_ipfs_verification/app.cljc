(ns quantfury-ipfs-verification.app
  (:require [clojure.java.shell :refer [sh]]
            #?(:bb [babashka.http-client :as http]
               :clj [clj-http.client :as http])
            [clojure.edn :as edn]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.string :as str]))

(def data-dir "data")
(def ipfs-url "https://ipfs.quantfury.com/ipfs/")

(defn calculate-results [csv-data]
  (reduce (fn [{:keys [num-trades sum-spread]} row]
            (let [spread (-> row (get 7) bigdec)]
              {:num-trades (inc num-trades)
               :sum-spread (+ sum-spread spread)}))
          {:num-trades 0, :sum-spread 0M}
          csv-data))

(defn process-csv [csv-file]
  (with-open [rdr (io/reader csv-file)]
    (->> rdr
         csv/read-csv
         rest
         calculate-results)))

(defn download-cid [cid]
  (let [url (str ipfs-url cid)
        zip-file (io/file data-dir (str cid ".zip"))]
    (when-not (.exists zip-file)
      (io/copy (:body (http/get url {:as :stream})) zip-file))
    zip-file))

(defn decrypt-file [file password]
  (let [temp-file (-> file
                       .getName
                       (str/split #"\.")
                       first
                       (java.io.File/createTempFile ".csv" ))
        command   ["7z" "x" "-y" "-so" (str "-p" password) (.getPath file)]]
    (.deleteOnExit temp-file)
    (with-open [writer (io/writer temp-file)]
      (.write writer (:out (apply sh command))))
    temp-file))

(defn close? [x y precision]
  (< (abs (- x y)) precision))

(defn process-cid-password [{:keys [cid password expected]}]
  (let [zip-file (download-cid cid)
        csv-file (decrypt-file zip-file password)
        results (process-csv csv-file)]
    (if (and (= (:num-trades expected)
                (:num-trades results))
             (close? (:sum-spread expected)
                     (:sum-spread results)
                     1e-2))
      (println "OK: " cid)
      (println "FAILED: " cid " Expected: " expected " Got: " results))))

(defn -main []
  (let [cids-passwords (edn/read-string (slurp "cids_passwords.edn"))]
    (doseq [cp cids-passwords]
      (process-cid-password cp))))

(comment

  (def temp-csv (-> "QmVB76oSA1GmxZWHseLiivrcfNE4ws2HhE5m7g491v8ENx"
                    download-cid
                    (decrypt-file "lb[732;.P!ULSMwbOEXF")))

  (with-open [rdr (io/reader temp-csv)]
    (->> rdr
         csv/read-csv
         rest
         (take 1000)
         calculate-results
         time))

  (.delete temp-csv)

  )
