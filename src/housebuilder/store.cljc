(ns housebuilder.store
  "SSoT for the ISCO-08 7111 house-building job-site scheduling/logistics
  coordination actor (itonami actor pattern, ADR-2607121000 / CLAUDE.md
  Actors section; README's 'Robotics premise' — a job-site
  scheduling/logistics coordination robot performs crew scheduling,
  task/materials-usage/progress-record logging and building-materials
  supply-order coordination for a house-building crew under this
  advisor/governor pair, which never dispatches hardware itself, never
  performs construction work itself, and never finalizes a
  structural-work-execution decision (e.g. a specific framing or
  foundation step) or overrides a site safety officer's/foreman's
  judgment — those remain the site safety officer's/foreman's
  exclusive judgment). Modeled on cloud-itonami-isco-3313's
  accountingsupport.store (and closely on cloud-itonami-isco-9311's
  mininglabor.store for the physical-safety-domain shape).

  Domain:

    builder — a registered house-building crew member
              (:builder-id, :name)
    site    — a registered house-building job site {:site-id :name
              :max-supply-cost number}. `:max-supply-cost` is an
              informational registered ceiling used only to decide
              whether a `:coordinate-supply-order` proposal escalates
              to human sign-off (the governor never blocks a
              within-threshold order outright; it only decides
              commit vs. escalate).
    record  — a committed operating record (a logged
              task/materials-usage/progress entry, a scheduled crew
              operation, a flagged safety concern, or a coordinated
              supply order) — written ONLY via commit-record!.
    ledger  — append-only audit trail, commit or hold.")

(defprotocol Store
  (builder [s builder-id])
  (site [s site-id])
  (records-of [s builder-id])
  (ledger [s])
  (register-builder! [s builder])
  (register-site! [s site])
  (commit-record! [s record])
  (append-ledger! [s fact]))

(defrecord MemStore [a]
  Store
  (builder [_ builder-id] (get-in @a [:builders builder-id]))
  (site [_ site-id] (get-in @a [:sites site-id]))
  (records-of [_ builder-id] (filter #(= builder-id (:builder-id %)) (:records @a)))
  (ledger [_] (:ledger @a))
  (register-builder! [s b]
    (swap! a assoc-in [:builders (:builder-id b)] b) s)
  (register-site! [s st]
    (swap! a assoc-in [:sites (:site-id st)] st) s)
  (commit-record! [s record]
    (swap! a update :records (fnil conj []) record) s)
  (append-ledger! [s fact]
    (swap! a update :ledger (fnil conj []) fact) s))

(defn mem-store
  ([] (mem-store {}))
  ([seed] (->MemStore (atom (merge {:builders {} :sites {} :records [] :ledger []}
                                    seed)))))
