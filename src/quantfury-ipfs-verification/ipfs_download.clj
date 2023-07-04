(ns quantufury-ipsf-verification.ipfs-download)

(defn download
  "Download a file from IPFS"
  [hash]
  (let [url (str "https://ipfs.io/ipfs/" hash)]
    (println "Downloading" url)
    (slurp url)))

