#!/usr/bin/env planck
(ns axe.lv1
  (:require [planck.core :refer [slurp]]
            [planck.http :as http]
            [clojure.string :as s :refer [replace,split]]))

(defn row->cols [row]
  (map #(replace % #"\s*(<td>|<tr>)" "")
       (split row #"</td>\s+")))

(defn result-set [rows]
  (let [[fname fs1 fs2 fs3 fs4 fs5] (row->cols (first rows))]
    (map
     (fn [row]
       (let [[rname rs1 rs2 rs3 rs4 rs5] (row->cols row)]
         {"name"   rname
          "grades" {fs1 (int rs1)
                    fs2 (int rs2)
                    fs3 (int rs3)
                    fs4 (int rs4)
                    fs5 (int rs5)}}))
     (rest rows))))

(def body (:body (http/get "http://axe-level-1.herokuapp.com/")))
(def data (last
           (re-find #"<table[^>]+>(.+)</table>"
                    (replace body #"\n" " "))))

(def rows (split data #"</tr>\s+"))

(println (.stringify js/JSON (clj->js (result-set rows))))
