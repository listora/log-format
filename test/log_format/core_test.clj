(ns log-format.core-test
  (:require [clojure.test :refer :all]
            [log-format.core :refer :all]))

(deftest test-metrics
  (are [m string] (= (metrics m) string)
       {:count {:a.b 1}} "count#a.b=1"
       {:measure {:a.b "5ms"}} "measure#a.b=5ms"
       {:sample {:a.b "10GB"}} "sample#a.b=10GB"

       {:count {:a.b 1} :measure {:a.b "5ms"} :sample {:a.b "10GB"}}
       "count#a.b=1 measure#a.b=5ms sample#a.b=10GB"

       {:count {:a.b 1 :a.c 2 :a.d 10}} "count#a.c=2 count#a.d=10 count#a.b=1"))

(defn- extract-metric-name [string]
  (second (re-find #"^(?:count|measure|sample)#(.*?)=" string)))

(deftest test-metric-names
  (doseq [metric [:count :measure :sample]]
    (are [metric-name expected]
      (= (extract-metric-name (metrics {metric {metric-name 1}})) expected)
         "metric" "metric"
         "a.metric" "a.metric"
         "a/metric" "a/metric"
         :metric "metric"
         :a.metric "a.metric"
         :a/metric "a.metric"
         :a.b.metric "a.b.metric"
         :a/b.metric "a.b.metric"
         :a.b/metric "a.b.metric"
         :a/b/metric "a/b.metric")))
