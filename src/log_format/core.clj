(ns log-format.core
  "Provides functions that generate log output ready for parsing by
  services like Librato.

  Returns a formatted log string, ready for you to log via your
  preferred logging solution."
  (:require [clojure.string :as str]
            [clojure.walk :as walk]))

(def ^:private metric-parts (juxt namespace name))
(def ^:private nil-key? (comp nil? second))

(defmulti ^:private name->string
  "Converts a metric name into a string, ready for printing to the log
  destination of your choice."
  class)

(defmethod name->string clojure.lang.Keyword
  [x]
  (->> x
       metric-parts
       (remove nil?)
       (str/join ".")))

(defmethod name->string String [x] x)

(defn- map-keys
  "Maps a function over the keys of an associative collection.

  Similar to medley.core/map-keys, but returns nil if the collection is
  nil."
  [f coll]
  (if (seq coll)
    (persistent! (reduce-kv #(assoc! %1 (f %2) %3)
                            (transient (empty coll))
                            coll))))

(defn- metric-key [prefix name]
  (str prefix "#" (name->string name)))

(defn- format-metrics [metric]
  (->> metric
       walk/stringify-keys
       (remove nil-key?)
       (map (partial str/join "="))
       (str/join " ")))

(defn- combine-metrics [{:keys [count measure sample]}]
  (concat
   (map-keys (partial metric-key "count") count)
   (map-keys (partial metric-key "measure") measure)
   (map-keys (partial metric-key "sample") sample)))

(defn metrics
  "Takes a hash-map of counts, measurements, samples, and an optional
  source, and returns a formatted string ready for logging.

  For example:

    (metrics {:count {:user.clicks 5}
              :measure {:db.query \"21ms\"}
              :sample {:db.size \"401GB\"}})

  …returns \"count#user.clicks=5 measure#db.query=21ms sample#db.size=401GB\"

  Any slashes in keyword metric names are replaced with full stops, and
  string metric names are returned as-is.

  So from the `user` namespace, calling metrics like so:

    (metrics {:count {::hits 4 \"i/know/best\" 11}})

  …returns \"count#i/know/best=11 count#user.hits=4\""
  [{:keys [count measure sample source]}]
  (->> (combine-metrics {:count count :measure measure :sample sample})
       (into {:source source})
       format-metrics))
