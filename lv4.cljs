#!/usr/bin/env lumo
(ns axe.lv4
  (:require[clojure.string :as s :refer [replace,split,join]]))

(def fs (js/require "fs"))
(def request (js/require "request"))

(def url "http://axe-level-4.herokuapp.com/lv4/")

(def headers
  {
   "User-Agent" "Mozilla/5.0 (Macintosh; Intel Mac OS X 20_22_26) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.98 Safari/637.56"
   "Accept" "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"
   "Referer" url})


(def re-columns-str
  (str "<tr>\\s*" (join (repeat 3 "<td>(.+)<\\/td>\\s*")) "<\\/tr>"))

(defn re-findall [re s]
  (let [re (js/RegExp. (or (.-source re) re) "g")]
    (loop [res []] (if-let [m (.exec re s)]
                     (recur (conj res (rest m)))
                     res))))

(defn body->rows [body]
  ((comp
    (fn [x]
      (map (fn [row]
             (let [[c1 c2 c3] row]
               {"town" c1 "village" c2 "name" c3}))
           (rest x)))
    (fn [x] (re-findall re-columns-str x)))
   body))

(def aEnd (atom false))
(def aUrl (atom '()))
(def aRes (atom '()))
(def aIdx (atom 0))
(def aRow (atom '()))
(def aExt (atom nil))
(def aRef (atom url))

(add-watch
 aUrl
 :w-aurl
 (fn [k r o n]
   (if (< (count o) (count n))
     (let [_url (first n)]
       (swap! aUrl rest)
       ;;(println "url:" _url)
       (if (re-find #"^http" _url)
         ; remote
         (request.
          (clj->js {"url" _url
                    "headers" (merge headers {"Referer" @aRef})
                    "method" "GET"})
          (fn [err, res, body]
            (if err
              ((println "err:" err)
               (reset! aEnd true)))
            (reset! aRef _url)
            (swap! aRes concat [body])))
         ; local
         (let [body (.readFileSync fs _url "utf8")]
           (swap! aRes concat [body])))))))

(add-watch
 aRes
 :w-ares
 (fn [k r o n]
   (if (< (count o) (count n))
     (let [_body (first n)]
       (if (nil? @aExt)
         (reset! aExt
                 (rest (map first (re-findall #"(\?page=\d+)" _body)))))
       (swap! aRes rest)
       ;;(println _body)
       (swap! aIdx inc)
       (swap! aRow concat (body->rows _body))
       (println "idx=" @aIdx " rows=" (count @aRow))
       (if (and (> (count @aExt) 0) (< @aIdx 25))
         ;; more data ... (str url _next) "axe/lv2.html"
         (let [_next (first @aExt)]
           (swap! aExt rest)
           (swap! aUrl concat [(str url _next)]))
         ;; no more
         (reset! aEnd true))))))

(add-watch
 aEnd :w-aend
 (fn [k r o n]
   (remove-watch aEnd :w-aend)
   (remove-watch aUrl :w-aurl)
   (remove-watch aRes :w-ares)
   (println "=========================================")
   (println (.stringify js/JSON (clj->js @aRow)))))

(swap! aUrl concat [url])
