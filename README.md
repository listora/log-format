# log-format

[![Build Status](https://travis-ci.org/listora/log-format.png?branch=master)](https://travis-ci.org/listora/log-format)
**log-format** helps generate log output ready for parsing by services
like Librato.

## Installation

[![Leiningen version](https://clojars.org/listora/log-format/latest-version.svg)](https://clojars.org/listora/log-format)

## Usage

Require the library and generate a string ready for logging:

``` clj
(require '[log-format.core :as lf])

(lf/metrics {:count {:wins 4 :losses 0}})
;; => "count#wins=4 count#losses=0"
```

Counts, measures, and samples are all supported out-of-the-box. For more
information on how Librato's log format works check out their
[Heroku devcenter article][more-info].

To reduce the amount of lines you output to your logs, you can build up
a hash-map of the metrics you want to log, and write them out just once
(say at the end of a web request).

To add new metrics you simply `assoc` them into the relevant part of
your hash-map, which might look something like:

``` clj
(require '[log-format.core :as lf])

(defn work [context]
  (-> context
     (assoc-in [:metrics :count :wins] 4)
     (assoc-in [:metrics :count :losses] 0)))

(defn handle [context]
  (let [return-value (work context)]
    (logger/debug (lf/metrics (:metrics return-value)))
    (dissoc :metrics return-value)))
```

## License

Copyright © 2014 Listora

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

[more-info]: https://devcenter.heroku.com/articles/librato
