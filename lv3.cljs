#!/usr/bin/env lumo
(ns axe.lv3
  (:require[clojure.string :as s :refer [replace,split,join]]))

(def fs (js/require "fs"))
(def request (js/require "request"))

(def url "http://axe-level-1.herokuapp.com/lv3/")

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

(defn sleep [msec]
  (let [deadline (+ msec (.getTime (js/Date.)))]
    (while (> deadline (.getTime (js/Date.))))))

(def jar (.jar request))

(def aEnd (atom false))
(def aUrl (atom '()))
(def aRes (atom '()))
(def aIdx (atom 0))
(def aRow (atom '()))

(add-watch
 aUrl
 :w-aurl
 (fn [k r o n]
   (if (< (count o) (count n))
     (let [_url (first n)]
       (swap! aUrl rest)
       ;;(println "url:" _url)
       (if (re-find #"^http" url)
         ; remote
         (request.
          (clj->js {"url" _url "jar" jar "method" "get"})
          (fn [err, res, body]
            (if err
              ((println "err:" err)
               (reset! aEnd true))
              (swap! aRes concat [body]))))
         ; local
         (let [body (.readFileSync fs _url "utf8")]
           (swap! aRes concat [body])))))))

(add-watch
 aRes
 :w-ares
 (fn [k r o n]
   (if (< (count o) (count n))
     (let [_body (first n)
           _next (last (re-find #"(\?page=next)" _body))]
       (swap! aRes rest)
       ;;(println _body)
       (swap! aIdx inc)
       (swap! aRow concat (body->rows _body))
       (println "idx=" @aIdx " rows=" (count @aRow))
       (if (and _next (< @aIdx 80))
         ;; more data ... (str url _next) "axe/lv2.html"
         (swap! aUrl concat [(str url _next)])
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
