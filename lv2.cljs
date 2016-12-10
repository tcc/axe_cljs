#!/usr/bin/env planck
(ns axe.lv2
  (:require [planck.http :as http]
            [planck.core :refer [slurp]]
            [clojure.string :as s :refer [replace,split,join]]))

(def url "http://axe-level-1.herokuapp.com/lv2/")

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

(defn get-body [res]
  (println res)
  (if (re-find #"^http" res)
    (:body (http/get res))
    (slurp res)))

(def body (get-body url))
(def p1-rows (body->rows body))
(def exts (rest (map first (re-findall #"(\?page=\d+)" body))))

(def all-rows
  (reduce (fn [rets ext] (concat rets
                                 (-> (str url ext)
                                     get-body
                                     body->rows)))
          p1-rows
          exts))

(println "=====================================")
(println (.stringify js/JSON (clj->js all-rows)))
