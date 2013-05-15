(import ei.ElectronicInstitution)

; set conflict resolution strategy to breadth, in order to get notification events in the same order as the occurrence of Jess facts
(set-strategy breadth)

; Java-implemented user functions - declared here to get rid of IDE warning messages
(deffunction schedule-time-alert ())
(deffunction norm-env-report ())

; execution modes

(deftemplate MAIN::execution-mode
    "To indicate the mode of execution of the normative environment"
    (slot type) )

(deffacts normal-mode
    "Normative environment running in normal-mode"
    (execution-mode (type normal)) )


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; TEMPLATES
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;;;;;;;;;;;;;;;;;;;;;;
;; Contexts

; The context definition slots are typically obtained from the negotiated xsd contract header only.
(deftemplate MAIN::context
    "Context for norms"
    (slot id
        (default-dynamic (+ (call System currentTimeMillis)
                            (call Double doubleToLongBits (* (call Math random) (get-member Long MAX_VALUE))) ) ) )
    (slot super-context (default INSTITUTIONAL-NORMS))
    (slot when (default-dynamic (call System currentTimeMillis)))
    (multislot who) )

(deftemplate MAIN::contextual-info
    "Contextual information"
    (slot context) )

;;;;;;;;;;;;;;;;;;;;;;;;
;; Institutional reality

(deftemplate MAIN::IRE
    "Institutional reality element"
    (declare (slot-specific TRUE))   ; because these elements are used both in rules and norms: since norms modify IREs, this
                                     ; prevents a rule from firing twice
    (slot context (default INSTITUTIONAL-NORMS))
    (slot when (default-dynamic (call System currentTimeMillis)))
    (slot processing-context)   ; used by context-handling rules
;    (slot tag   ; allow for different ire's to have the same contents (slot 'when' almost gives me that, but it doesn't)
;        (default-dynamic (+ (call System currentTimeMillis)
;                            (call Double doubleToLongBits (* (call Math random) (get-member Long MAX_VALUE))) ) ) )
    )
; (17/10/2008: came to this comment after struggling with duplicate time events...)
; NOTE that the modification of the 'processing-context' by rules makes it possible to assert ires that are equal in every other slot, which
; may give some sense of randomness when accidently asserting 'equal' ires (it depends on whether the processing-context modifying rules fire
; before or after the new assert). Therefore, I cannot assume that an ire assert will fail if there is already a similar ire in working memory.
; (For the problem I had with time events, I am now more careful - see new-time-event deffunction.)

(deftemplate MAIN::start-context extends IRE
    "To indicate that a context has started" )

; The situations for ending a context are defined with norms, benefiting from the norm-inheritance mechanism: norms can assert
; 'end-context' just as they can assert obligations
; The 'end-context' facts are checked in constitutive rules: ended contexts cannot get further institutional facts. Ended contexts
; are also inhibited from processing ire's.
(deftemplate MAIN::end-context extends IRE
    "To indicate that a context has ended" )

(deftemplate MAIN::active-context
    "To indicate that a context is active"
    (slot context) )

(defrule MAIN::active-context-rule
    "Active context state"
    (logical (start-context (context ?ctx)))
    (logical (not (end-context (context ?ctx))))
    =>
    (assert (active-context (context ?ctx))) )

(deftemplate MAIN::ifact extends IRE
    "Institutional fact"
    (multislot fact) )   ; ontology for institutional-facts goes here...

;; NOTE: a separate fact for the multislot would require a change in the way obligations are handled, as it would not be possible
;; anymore to directly match the obligation's fact with the ifact's fact. Adding structure to the facts themselves requires adding
;; structure also inside the obligations.
;; 
;; Another approach would be to fill the 'fact' slot with a Java object, relying on equals() for matching obligation facts with ifacts facts!
;; Multislot fact in both obligation and ifact would be an object instance of a subclass of an abstract 'fact' class.
;; Rationale: templates that need ontologies for brute or institutional facts could include Java objects, this way avoiding multislots.
;; - Define 3 subclasses for each kind of brute fact slot and for each kind of institutional fact slot?
;; --> See section 8.4 of Jess manual.
;;
;; Note that any changes here demand modifications at XMLContract2Jess...
;;
;; For now I will stick to having a multilslot 'fact'.

(deftemplate MAIN::time extends IRE
    "Time event" )

(deftemplate MAIN::obligation extends IRE
    "Obligation"
    (slot id)   ; id of this obligation within the contract-type where it is prescribed
    (slot bearer)
    (slot counterparty)
    (multislot fact)   ; ontology for institutional-facts goes here...
    (slot liveline (default 0))   ; 0 indicates no liveline (there is no way the obliged fact is achieved before this)
    (slot deadline)
    (slot fine (default 0.0))   ; a regimented sanction (introduced for MASTA@EPIA2011 paper - unsubmitted paper with Sérgio)
    (slot processed (default FALSE)) )   ; for fulfillment/violation detection

(deftemplate MAIN::liveline-violation extends IRE
    "Liveline violation of an obligation"
    (slot obl)   ; a reference to the obligation whose liveline was violated
    (slot ifa)   ; a reference to the institutional fact raising the liveline violation
    (slot processed (default FALSE)) )   ; for fulfillment/violation detection

(deftemplate MAIN::deadline-violation extends IRE
    "Deadline violation of an obligation"
    (slot obl)   ; a reference to the obligation whose deadline was violated
    (slot processed (default FALSE)) )   ; for fulfillment/violation detection

(deftemplate MAIN::fulfillment extends IRE
    "Obligation fulfillment"
    (slot obl)   ; a reference to the fulfilled obligation
    (slot ifa) )   ; a reference to the fulfilling institutional fact

(deftemplate MAIN::violation extends IRE
    "Obligation violation"
    (slot obl) )   ; a reference to the violated obligation

(deftemplate MAIN::denounce extends IRE
    "Temporal violation denouncement"
    (slot obl) )   ; a reference to the temporally violated obligation

;;;;;;;;;;;;;;;;;;;;;;;;
;; Brute facts

(deftemplate MAIN::bfact
    "Brute fact"
    (slot agent)
    (slot context)   ; if I change the statement slot so that it contains a Java object (see comment above on ifact), maybe this information goes inside it 
    (multislot statement)   ; ontology for brute-facts goes here...
    (slot when) )   ; if I change the statement slot so that it contains a Java object (see comment above on ifact), maybe this information goes inside it

;;;;;;;;;;;;;;;;;;;;;;;;
;; Role enacting agents (social-contract)

(deftemplate MAIN::rea extends context
    "Role enacting agent - agent takes a role"
    (slot agent)   ; this slot might be unnecessary, since the agent name is already at context's 'who' slot
    (slot role) )
;; For simplification, rea data is inside the context definition. Note that a rea is not obtained from a negotiated xsd contract
;; (when negotiated, context definition slots are obtained from the contract header only).

; Since norms can be defined for rea, a better name would be social-contract. Although for now I am not using norms in social-contracts,
; this definition is kept because removing it would imply many changes (ako backward compatibility... :p)
(deftemplate MAIN::social-contract extends rea
    "Social contract - agent takes a role" )


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; CONSTITUTIVE RULES
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; Note: here I *mainly* define BF -> IF rules; rules of the kind IF -> IF are considered institutional rules, and can be defined
; inside contexts (the contract xsd includes such rules).

(defmodule CONSTITUTIVE-RULES
    "Constitutive rules"
    (declare (auto-focus TRUE)) )

; The (not (ifact ...)) in these rules is meant to prevent the rules from firing with platform resuming, where agents/roles are
; added but bfacts might be already in the loaded .bsave file.
; Another option would be to simply remove or mark bfacts as processed, but this would prevent a bfact from being used in more than one
; constitutive rule...

(defrule CONSTITUTIVE-RULES::payments_synchronized-experiment-mode
    "Constitutive rule for acknowledging payments"
    (execution-mode (type sync-experiment))
    (bfact (agent ?a) (context ?ctx) (statement currency-transfered $?data) (when ?wh))   ;; TODO
    (active-context (context ?ctx))   ; check that context is active
    (not (ifact (context ?ctx) (fact payment $?data) (when ?wh)))   ; assuming there will be no equal bfacts
    =>
    (bind ?ifa (assert (ifact (context ?ctx) (fact payment $?data) (when ?wh)))) )

(defrule CONSTITUTIVE-RULES::deliveries_synchronized-experiment-mode
    "Constitutive rule for acknowledging deliveries"
    (execution-mode (type sync-experiment))
    (bfact (agent ?a) (context ?ctx ) (statement delivered $?data) (when ?wh))   ;; TODO
    (active-context (context ?ctx))   ; check that context is active
    (not (ifact (context ?ctx) (fact delivery $?data) (when ?wh)))   ; assuming there will be no equal bfacts
    =>
    (bind ?ifa (assert (ifact (context ?ctx) (fact delivery $?data) (when ?wh))))
;    (printout t "(+) New ifact: " (?ifa toString) crlf)
    )

(defrule CONSTITUTIVE-RULES::msg-deliveries_synchronized-experiment-mode
    "Constitutive rule for acknowledging msg-deliveries"
    (execution-mode (type sync-experiment))
    (bfact (agent ?a) (context ?ctx) (statement msg-delivered $?data) (when ?wh))   ;; TODO
    (active-context (context ?ctx))   ; check that context is active
    (not (ifact (context ?ctx) (fact msg-delivery $?data) (when ?wh)))   ; assuming there will be no equal bfacts
    =>
    (bind ?ifa (assert (ifact (context ?ctx) (fact msg-delivery $?data) (when ?wh))))
;    (printout t "(+) New ifact: " (?ifa toString) crlf)
    )

(defrule CONSTITUTIVE-RULES::payments
    "Constitutive rule for acknowledging payments"
    (bfact (agent ?a) (context ?ctx) (statement currency-transfered $?data) (when ?wh))   ;; TODO
    (active-context (context ?ctx))   ; check that context is active
    (social-contract (agent ?a) (role ?r))   ; TODO: should ckeck if this context is active as well!
    (test (eq ?r (sym-cat (ElectronicInstitution.ROLE_BANK))))
    (not (ifact (context ?ctx) (fact payment $?data) (when ?wh)))   ; assuming there will be no equal bfacts
    =>
    (bind ?ifa (assert (ifact (context ?ctx) (fact payment $?data) (when ?wh))))
;    (printout t "(+) New ifact: " (?ifa toString) crlf)
    )

(defrule CONSTITUTIVE-RULES::deliveries
    "Constitutive rule for acknowledging deliveries"
    (bfact (agent ?a) (context ?ctx ) (statement delivered $?data) (when ?wh))   ;; TODO
    (active-context (context ?ctx))   ; check that context is active
    (social-contract (agent ?a) (role ?r))   ; TODO: should ckeck if this context is active as well!
    (test (eq ?r (sym-cat (ElectronicInstitution.ROLE_DELIVERY_TRACKER))))
    (not (ifact (context ?ctx) (fact delivery $?data) (when ?wh)))   ; assuming there will be no equal bfacts
    =>
    (bind ?ifa (assert (ifact (context ?ctx) (fact delivery $?data) (when ?wh))))
;    (printout t "(+) New ifact: " (?ifa toString) crlf)
    )

(defrule CONSTITUTIVE-RULES::msg-deliveries
    "Constitutive rule for acknowledging msg-deliveries"
    (bfact (agent ?a) (context ?ctx) (statement msg-delivered $?data) (when ?wh))   ;; TODO
    (active-context (context ?ctx))   ; check that context is active
    (social-contract (agent ?a) (role ?r))   ; TODO: should ckeck if this context is active as well!
    (test (eq ?r (sym-cat (ElectronicInstitution.ROLE_MESSENGER))))
    (not (ifact (context ?ctx) (fact msg-delivery $?data) (when ?wh)))   ; assuming there will be no equal bfacts
    =>
    (bind ?ifa (assert (ifact (context ?ctx) (fact msg-delivery $?data) (when ?wh))))
;    (printout t "(+) New ifact: " (?ifa toString) crlf)
    )

; denouncements
(defrule CONSTITUTIVE-RULES::denounces
    "Acknowledging denounces"   ; a constitutive rule based on ifacts - this kind of rules may be contextualized!
    (ifact (context ?ctx) (fact msg-delivery ref ? from ?fr to ?to msg denounce $?fact) (when ?wh))
    ?obl <- (obligation (context ?ctx) (bearer ?to) (counterparty ?fr) (fact $?fact))
    =>
    (bind ?den (assert (denounce (context ?ctx) (obl ?obl) (when ?wh))))
;    (printout t "(+) New denounce: " (?den toString) crlf)
    )

; make it easy to process messages of a certain type: messages are assumed to start with the message type
; -> an ifact with a fact slot begining with the message type will be created
; -> note however that obligation prescribed facts will be msg-deliveries (or are at least for now)...
; -> it might be cleaner to treat all cases individually (as with denounces)
(defrule CONSTITUTIVE-RULES::msg-types
    "Acknowledging a message type (orders, ...)"   ; a constitutive rule based on ifacts - this kind of rules may be contextualized!
    (ifact (context ?ctx) (fact msg-delivery ref ?ref from ?fr to ?to msg ?msg-type $?msg-contents) (when ?wh))
    =>
    (bind ?ifa (assert (ifact (context ?ctx) (fact ?msg-type ref ?ref from ?fr to ?to $?msg-contents) (when ?wh))))
;    (printout t "(+) New ifact: " (?ifa toString) crlf)
    )


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; INSTITUTIONAL RULES
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmodule INSTITUTIONAL-RULES
    "Institutional rules"
    (declare (auto-focus TRUE)) )

;;;;;;;;;;;;;;;;;;;;;;;;
;; contract starting

(defrule INSTITUTIONAL-RULES::schedule-start-time-alert
    "Schedule a time event corresponding to the start of a context's life"
;    (execution-mode (type normal))
    (context (id ?ctx) (when ?w))
    =>
    (schedule-time-alert ?ctx ?w) )

(defrule INSTITUTIONAL-RULES::context-start
    "Signal the start of a context"
;    (execution-mode (type normal))
    (context (id ?ctx) (when ?w))
    (time (context ?ctx) (when ?w))
    =>
    (assert (start-context (context ?ctx) (when ?w))) )

;;;;;;;;;;;;;;;;;;;;;;;;;
;;; time patterns
;
;(do-backward-chaining time)
;
;(defrule do-time
;    "Back-chaining rule for time events: when a time pattern is needed, schedule a time event"
;    (need-time (context ?ctx) (when ?w))
;    =>
;    (schedule-time-alert ?ctx ?w) )
;
;(deftemplate k
;    (slot when (default-dynamic (call System currentTimeMillis))) )
;
;(defrule r
;    (k (when ?w))
;    (time (when =(+ ?w 3000)))   ; 	DOES NOT COMPILE WITH BAK-CHAINING PATTERNS
;    =>
;    (printout t "GOT IT: " ?w " : " crlf) )

;;;;;;;;;;;;;;;;;;;;;;;;
;; semantics of directed obligations with livelines and deadlines

(defrule INSTITUTIONAL-RULES::schedule-liveline-time-alert
    "Schedule a time event at an obligation's liveline"
;    (execution-mode (type normal))
    ?obl <- (obligation {liveline > 0})   ; only if there is a liveline defined
    =>
;    (printout t "(O) Scheduling time-alert for " ?obl.context " at " ?obl.liveline crlf)
    (schedule-time-alert ?obl.context ?obl.liveline) )

(defrule INSTITUTIONAL-RULES::schedule-deadline-time-alert
    "Schedule a time event at an obligation's deadline"
;    (execution-mode (type normal))
    ?obl <- (obligation)
    =>
;    (printout t "(O) Scheduling time-alert for " ?obl.context " at " ?obl.deadline crlf)
    (schedule-time-alert ?obl.context ?obl.deadline) )

; * On belated ifacts *
; The monitoring model assumes an instant recognition of each IRE. That is, an ifact occuring at time t is added at instant t to the
; normative state (no ifact will be added with a past 'when' slot). Since this can be difficult or counterproductive to ensure, we
; need to prevent possible problems. What we need is to say that if belated facts can occur, a violated obligation cannot be later
; fulfilled, as this introduces inconsistencies into the system -- *I am not dealing with "belief revision" yet!*
; The obligation monitoring model implemented will ignore belated ifacts (with a past 'when' slot) matching already violated
; obligations. This prevents a violated obligation from being later fulfilled. (See "Practical issues" in COIN@AAMAS2008 paper.)
; --> The implemented solution is to mark obligations with a (processed TRUE) slot as they are fulfilled/violated. Obligations with
;     (processed TRUE) will never be temporally-violated / fulfilled / violated again.
;     The same approach is used for liveline and deadline violations.
; An alternative solution would be to check if an obligation was violated before asserting it as fulfilled. However, this would
; jeoperdize the independence of both rules, although it would make some sense as a patch.

(defrule INSTITUTIONAL-RULES::detect-liveline-violation
    "Detect a livelive violation of an obligation"
    ; Rationale:	IF the obliged fact is brought about before the liveline,
    ; 				THEN there is a liveline violation
    ?obl <- (obligation (context ?c) (fact $?f) (liveline ?liveline) (processed FALSE))
    ?ifa <- (ifact (context ?c) (fact $?f) {when < ?liveline})
    =>
    (assert (liveline-violation (context ?c) (when ?ifa.when) (obl ?obl) (ifa ?ifa)))
    (modify ?obl (processed TRUE)) )   ; liveline-violated state: the obligation cannot be liveline-violated again nor fulfilled directly (avoid hazard of having 2 ifacts for the same obligation)

(defrule INSTITUTIONAL-RULES::detect-early-fulfillment
    "Detect an early fulfillment of an obligation, at the liveline"
    ; Rationale:	IF there is a liveline violation and the liveline occurs before any denouncement,
    ; 				THEN the obligation is fulfilled at the liveline
    ?lviol <- (liveline-violation (context ?c) (obl ?obl) (processed FALSE))
    ?obl <- (obligation (context ?c) (liveline ?liveline))
    ; livelive before denouncement
    (time (context ?c) (when ?liveline))
    (not (denounce (context ?c) (obl ?obl) {when <= ?liveline}))
    =>
    (assert (fulfillment (context ?c) (when ?liveline) (obl ?obl) (ifa ?lviol.ifa)))
    (modify ?lviol (processed TRUE)) )   ; fulfilled state: the obligation cannot be violated anymore (avoid hazard of having a delayed denounce)

(defrule INSTITUTIONAL-RULES::detect-violation-before-liveline
    "Detect the violation of an obligation before the liveline"
    ; Rationale:	IF there is a liveline violation and a denouncement is made before the liveline,
    ; 				THEN the obligation is violated
    ?lviol <- (liveline-violation (context ?c) (obl ?obl) (processed FALSE))
    ?obl <- (obligation (context ?c) (liveline ?liveline))
    ; denouncement before liveline
    ?den <- (denounce (context ?c) (obl ?obl) {when < ?liveline})
    =>
    (assert (violation (context ?c) (when ?den.when) (obl ?obl)))
    (modify ?lviol (processed TRUE)) )   ; violated state: the obligation cannot be violated again (avoid hazard of having 2 denounces for the same obligation)

(defrule INSTITUTIONAL-RULES::detect-fulfillment
    "Detect the fulfillment of an obligation"
    ; Rationale:	IF the obliged fact is brought about between the liveline and the deadline,
    ; 				THEN the obligation is fulfilled
    ?obl <- (obligation (context ?c) (fact $?f) (liveline ?liveline) (deadline ?deadline) (processed FALSE))
    ?ifa <- (ifact (context ?c) (fact $?f) {when >= ?liveline && when <= ?deadline})
    =>
    (assert (fulfillment (context ?c) (when (max ?ifa.when ?obl.when))   ; assuming that the ifact can arrive before the obligation...
                (obl ?obl) (ifa ?ifa) ) )
    (modify ?obl (processed TRUE)) )   ; fulfilled state: the obligation cannot be fulfilled again (avoid hazard of having 2 ifacts for the same obligation)

(defrule INSTITUTIONAL-RULES::detect-deadline-violation
    "Detect a deadline violation of an obligation"
    ; Rationale:	IF the obliged fact was not brought about by the deadline,
    ; 				THEN there is a deadline violation
    ?obl <- (obligation (context ?c) (fact $?f) (deadline ?deadline) (processed FALSE))
    (time (context ?c) (when ?deadline))
    (not (ifact (context ?c) (fact $?f) {when <= ?deadline}))
    =>
    (assert (deadline-violation (context ?c) (when ?deadline) (obl ?obl)))
    (modify ?obl (processed TRUE)) )   ; deadline-violated state: the obligation cannot be fulfilled directly (avoid hazard of having a delayed ifact)

(defrule INSTITUTIONAL-RULES::detect-belated-fulfillment
    "Detect a belated fulfillment of an obligation"
    ; Rationale:	IF there is a deadline violation and the obliged fact is brought about before any denouncement,
    ; 				THEN the obligation is fulfilled
    ?dviol <- (deadline-violation (context ?c) (obl ?obl) (processed FALSE))
    ?obl <- (obligation (context ?c) (fact $?f))
    ; ifact before denouncement
    ?ifa <- (ifact (context ?c) (fact $?f) (when ?when))
    (not (denounce (context ?c) (obl ?obl) {when <= ?when}))
    =>
    (assert (fulfillment (context ?c) (when ?when) (obl ?obl) (ifa ?ifa)))
    (modify ?dviol (processed TRUE)) )   ; fulfilled state: the obligation cannot be violated anymore (avoid hazard of having a delayed denounce)
                                         ;                  nor fulfilled again (avoid hazard of having 2 ifacts for the same obligation)

(defrule INSTITUTIONAL-RULES::detect-violation-after-deadline
    "Detect the violation of an obligation after the deadline"
    ; Rationale:	IF there is a deadline violation and a denouncement is made before the obliged fact is brought about,
    ; 				THEN the obligation is violated
    ?dviol <- (deadline-violation (context ?c) (obl ?obl) (processed FALSE))
    ?obl <- (obligation (context ?c) (fact $?f))
    ; denouncement before ifact
    (denounce (context ?c) (obl ?obl) (when ?when))
    (not (ifact (context ?c) (fact $?f) {when < ?when}))
    =>
    (assert (violation (context ?c) (when ?when) (obl ?obl)))
    (modify ?dviol (processed TRUE)) )   ; violated state: the obligation cannot be fulfilled (avoid hazard of having a delayed ifact)
                                         ;                 nor violated again (avoid hazard of having 2 denounces for the same obligation)

;;;;;;;;;;;;;;;;;;;;;;;;
;; reports

(defrule INSTITUTIONAL-RULES::new-contract-report
    "Report a new contract"
;    (execution-mode (type normal))
    ?ctxDef <- (context)
    =>
    (printout t crlf "(*) New contract/context: " (call ?ctxDef getFactId) ":" ?ctxDef.id " " ?ctxDef.who crlf crlf)
    (norm-env-report ?ctxDef) )

(defrule INSTITUTIONAL-RULES::ire-report
    "Report an IRE"   ; note that 'start-context' and 'end-context' are IREs
;    (execution-mode (type normal))
    ?ire <- (IRE)
    =>
    (printout t "(>) New IRE: " (call ?ire getFactId) ":" (call ?ire toString) crlf)
    (norm-env-report ?ire) )


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; PROCESSING-CONTEXT MANAGEMENT RULES
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; The implemented approach is to mark each IRE with the name of the processing-context, that is, the context where norms can be
; applied to that IRE. Initially, the processing-context is the IRE's context. The processing-context goes up one level to its
; super-context, until the INSTITUTIONAL-NORMS context is crossed. Norms that use this IRE can be applied in any context on the way,
; provided that they do not apply on the same state fraction.
; The same is true for contextual constitutive rules: they may be found at any context along the way. In this case there is no
; defeasibility model to implement (all the rules do is to assert new ifacts).

; this first rule is just a "listner" for new IRE's; it wouldn't be necessary if IRE's got the right processing-context at birth...
; - but this seems to be possible only by using JessEvents, not directly in the deftemplate (see e-mail exchanged with ejfried)
(defrule MAIN::new-ire-processing-context
    "When a new IRE is created, set its processing-context"
    ?ire <- (IRE (context ?ctx) (processing-context nil))
    (active-context (context ?ctx))   ; check that context is active
    =>
;    (printout t "Setting processing-context of " ?ire " to " ?ctx crlf)
    (modify ?ire (processing-context ?ctx)) )

(defrule MAIN::update-ire-processing-context
    "Update IRE processing-context"
    (declare (salience -100))   ; make sure this rule fires last (after norms)
    ?ire <- (IRE (processing-context ?pc))
    (context (id ?pc) (super-context ?sc))
    =>
;    (printout t "Updating processing-context of " ?ire " up to " ?sc crlf)
    (modify ?ire (processing-context ?sc)) )

(defrule MAIN::finish-ire-processing-context
    "Finish IRE processing-context"
    (declare (salience -100))   ; make sure this rule fires last (after norms)
    ?ire <- (IRE (processing-context INSTITUTIONAL-NORMS))
    =>
;    (printout t "Finishing processing-context of " ?ire crlf)
    (modify ?ire (processing-context VOID)) )   ; or any other processing-context that is neither nil nor a contract module


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; INSTITUTIONAL NORMS (TOP LEVEL "DEFAULT RULES")
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftemplate MAIN::norm-fired-on
    "To register the state fractions (IREs) on which norms fire"
    (multislot ires)   ; the IRE's on which a norm fired
    (slot context) )   ; the context of the firing norm (necessary because I must allow multiple firings in the same module!)

; Principles to follow when writing norms:
; - at least one IRE in the norm's LHS must have as processing-context the norm's module
; - there should be no 'norm-fired-on' fact on the same IRE's and with a different context
; - when firing, the norm should assert a norm-fired-on fact with the IRE's on which it fired
; - every module for norms has auto-focus enabled
;
; Accordingly, the general structure of a norm is:
;(defrule <module-name>::a-norm
;    "Brief description of the norm"
;    (context (id ?ctx) ...)   ; context-definition where the norm applies (any context type extending 'context' may be used, and ?ctx may be unbound)
;    (contextual-info (context ?ctx) ...)   ; contextual-info patterns
;    ...
;    ?ire_1 <- (IRE (context ?ctx) ... (processing-context ?pc_1))
;    ...
;    ?ire_n <- (IRE (context ?ctx) ... (processing-context ?pc_n))
;    ...   ; relational conditions over variables
;    ; check that at least 1 IRE has this module as its processing-context
;    (test (member$ <module-name> (list ?pc_1 ... ?pc_n)))
;    		; when having only 1 IRE, this test can be done directly in the pattern
;    ; check that there is no norm-fired-on with these IREs and referring to another context
;    (not (norm-fired-on (ires $?ires&:(equal-sets? ?ires (list ?ire_1 ... ?ire_n))) (context ~<module-name>)))
;    		; (not (norm-fired-on (ires ?ire_1 ... ?ire_n) (context ~<module-name>))) would be more efficient, although it only enforces
;    		; one ordering of ire's; when having only 1 IRE, this approach is better
;    =>
;    (assert (obligation (context ?ctx) ...))   ; or end-context
;    ...
;    ; save norm-fired-on information
;    (assert (norm-fired-on (ires ?ire_1 ... ?ire_n) (context <module-name>))) )

;;;;;;;;;;;;;;;;;;;;;;;;
;; INSTITUTIONAL-NORMS module definition

; INSTITUTIONAL-NORMS is the top context in which default norms regarding each type of context will be defined. These norms can be
; inherited by specific contexts of these types, if they are not defeated by other norms.
(defmodule INSTITUTIONAL-NORMS
    "Institutional norms (default rules defined at the top institutional context)"
    (declare (auto-focus TRUE)) )


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; MISCELANEOUS FUNCTIONS AND QUERIES
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; deffunction equal-sets? - used by norms
(deffunction equal-sets? (?list1 ?list2)
    "Test if two lists contain the same elements, in any order"
    ; this implementation requires that both lists are sets, that is, have no duplicate elements
    ; if not, must check if union$ and intersection$ obtain lists with the same length$
    (return (= (length$ ?list1) (length$ ?list2) (length$ (union$ ?list1 ?list2)))) )

; defquery get-social-contracts - used by NormEnvBehaviour
(defquery MAIN::get-social-contracts
    "Get an agent's social contracts"
    (declare (variables ?agent))
    (social-contract (id ?id) (agent ?agent) (role ?role))
    (active-context (context ?id)) )   ; check that context is active

; deffunction end-social-contract - used by NormEnvBehaviour
(deffunction end-social-contract (?id)
    "End a social contract"
    (assert (end-context (context ?id))) )

; deffunction end-social-contracts - not being used for now
(deffunction end-social-contracts (?agent)
    "End an agent's social contracts"
    (bind ?result (run-query* get-social-contracts ?agent))
    (while (?result next)
        (assert (end-context (context (?result getSymbol id)))) ) )

; deffunction set-execution-mode - used for synchronized experiments
(deffunction set-execution-mode (?mode)
    "Sets the mode of operation"
    (remove MAIN::execution-mode)
    (assert (MAIN::execution-mode (type ?mode))) )

; deffunction show-context-ires - usable from the normative environment GUI
(defquery MAIN::get-context-ires
    "Get IREs for a given context"
    (declare (variables ?ctx))
    ?ire <- (IRE (context ?ctx)) )
(deffunction show-context-ires (?ctx)
    "Print IREs for a given context"
    (bind ?result (run-query* get-context-ires ?ctx))
    (while (?result next)
        (printout t (?result get ire) ": " ((?result get ire) toString) crlf)) )

; defquery get-context-def - used by NormEnvEngine
(defquery MAIN::get-context-def
    "Get the context definition for a given contract"
    (declare (variables ?id))
    ?context-def <- (context (super-context ?super-context) (id ?id) (when ?when) (who $?who)) )

; defquery get-contextual-infos - used by JessNormEnvReport
(defquery MAIN::get-contextual-infos
    "Get the contextual-infos for a given contract"
    (declare (variables ?id))
    ?contextual-info <- (contextual-info (context ?id)) )

; deffunction new-time-event - used by NormEnvBehaviour
(defquery MAIN::get-time-event
    "Get a time event"
    (declare (variables ?context ?when))
    (time (context ?context) (when ?when)) )
(deffunction new-time-event (?context ?when)
    "Assert a time event if it does not already exist"   ; TODO: maybe should check that the context is active
    (if (= (count-query-results get-time-event ?context ?when) 0) then
        (assert (time (context ?context) (when ?when))) ) )

; template and fact to enable scheduling time-events at norm's pattern matching phase
(deftemplate time-scheduler
    (declare (ordered TRUE)))
(deffacts time-scheduler-fact
    (time-scheduler _) )
; this fact enables using (time-scheduler :(schedule-time-alert <ctx> <when>)) in norms immediately before the (time) pattern,
; which reduces the number of generated (time) facts, since these are added in the pattern matching phase (before norm activation)


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; CONTEXT TYPES - DEFINITIONS AND DEFAULT RULES (NORMS)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;;;;;;;;;;;;;;;;;;;;;;
;; General contract: base template for contracts that are obtained through negotiation

(deftemplate MAIN::contract extends context
    "Base template for contracts" )


;;;;;;;;;;;;;;;;;;;;;;;;
;; Empty contract: nothing to do

(deftemplate MAIN::_empty-contract extends contract
    "Empty contract" )

(defrule INSTITUTIONAL-NORMS::_empty-contract_void
    "This contract immediately ends after starting.
    	start --> end"
    (_empty-contract (id ?ctx))
    ?st <- (start-context (context ?ctx) (when ?w) (processing-context INSTITUTIONAL-NORMS))
    (not (norm-fired-on (ires ?st) (context ~INSTITUTIONAL-NORMS)))
    =>
    (assert (end-context (context ?ctx) (when ?w)))
    (assert (norm-fired-on (ires ?st) (context INSTITUTIONAL-NORMS))) )


;;;;;;;;;;;;;;;;;;;;;;;;
;; As simple as it gets contract

; context definition

(deftemplate MAIN::asaig extends contract
    "A contract with a single obligation to supply the product" )

; contextual-info definitions

(deftemplate MAIN::asaig-data extends contextual-info
    "Data for asaig"
    (slot seller)
    (slot buyer)
    (slot product)
    (slot quantity (default 1))
    (slot unit-price (default 0.0))
    (slot delivery-rel-deadline (default 5000)) )

; default rules

(defrule INSTITUTIONAL-NORMS::asaig_N1_delivery
    "The contract starts with the seller obligation to deliver the product.
    	start --> Obl(delivery)"
    (asaig (id ?ctx))
    (asaig-data (context ?ctx) (seller ?s) (buyer ?b) (product ?p) (quantity ?q)
        (delivery-rel-deadline ?drd) )
    ?st <- (start-context (context ?ctx) (when ?w) (processing-context INSTITUTIONAL-NORMS))
    (not (norm-fired-on (ires ?st) (context ~INSTITUTIONAL-NORMS)))
    =>
    (assert (obligation (id delivery) (context ?ctx) (bearer ?s) (counterparty ?b) (deadline (+ ?w ?drd))
                (fact delivery ref ?ctx from ?s to ?b product ?p quantity ?q) ) )
    (assert (norm-fired-on (ires ?st) (context INSTITUTIONAL-NORMS))) )

(defrule INSTITUTIONAL-NORMS::asaig_N2_no-delivery
    "A delivery violation leads to end of contract.
    	Viol(delivery) --> end"
    (asaig (id ?ctx))
    ?viol <- (violation (context ?ctx) (obl ?obl) (when ?w) (processing-context INSTITUTIONAL-NORMS))
    ?obl <- (obligation (context ?ctx) (fact delivery $?) )
    (not (norm-fired-on (ires ?viol) (context ~INSTITUTIONAL-NORMS)))
    =>
    (assert (end-context (context ?ctx) (when ?w)))
    (assert (norm-fired-on (ires ?viol) (context INSTITUTIONAL-NORMS))) )

(defrule INSTITUTIONAL-NORMS::asaig_N3_end-of-contract
    "When the delivery has been fulfilled, an end of contract is reached.
    	Fulf(delivery) --> end"
    (asaig (id ?ctx))
    ?ff <- (fulfillment (context ?ctx) (obl ?obl_del) (when ?w) (processing-context INSTITUTIONAL-NORMS))
    ?obl_del <- (obligation (context ?ctx) (fact delivery ref ?ctx $?))
    (not (norm-fired-on (ires ?ff) (context ~INSTITUTIONAL_NORMS)) )
    =>
    (assert (end-context (context ?ctx) (when ?w)))
    (assert (norm-fired-on (ires ?ff) (context INSTITUTIONAL-NORMS))) )


;;;;;;;;;;;;;;;;;;;;;;;;
;; As simple as it gets contract with regimented sanction

; context definition

(deftemplate MAIN::asaig-reg extends contract
    "A contract with a single obligation to supply the product, plus a regimented sanction" )

; contextual-info definitions

(deftemplate MAIN::asaig-reg-data extends asaig-data
    "Data for asaig-reg"
    (slot fine (default 0.0)) )

; default rules

(defrule INSTITUTIONAL-NORMS::asaig-reg_N1_delivery
    "The contract starts with the seller obligation to deliver the product.
    	start --> Obl(delivery)"
    (asaig-reg (id ?ctx))
    (asaig-reg-data (context ?ctx) (seller ?s) (buyer ?b) (product ?p) (quantity ?q)
        (delivery-rel-deadline ?drd) (fine ?fine) )
    ?st <- (start-context (context ?ctx) (when ?w) (processing-context INSTITUTIONAL-NORMS))
    (not (norm-fired-on (ires ?st) (context ~INSTITUTIONAL-NORMS)))
    =>
    (assert (obligation (id delivery) (context ?ctx) (bearer ?s) (counterparty ?b) (deadline (+ ?w 4000));?drd))
                (fact delivery ref ?ctx from ?s to ?b product ?p quantity ?q) (fine ?fine) ) )
    (assert (norm-fired-on (ires ?st) (context INSTITUTIONAL-NORMS))) )

(defrule INSTITUTIONAL-NORMS::asaig-reg_N2_no-delivery
    "A delivery violation leads to end of contract.
    	Viol(delivery) --> end"
    (asaig-reg (id ?ctx))
    ?viol <- (violation (context ?ctx) (obl ?obl_del) (when ?w) (processing-context INSTITUTIONAL-NORMS))
    ?obl_del <- (obligation (context ?ctx) (fact delivery $?) )
    (not (norm-fired-on (ires ?viol) (context ~INSTITUTIONAL-NORMS)))
    =>
    (assert (end-context (context ?ctx) (when ?w)))
    (assert (norm-fired-on (ires ?viol) (context INSTITUTIONAL-NORMS))) )

(defrule INSTITUTIONAL-NORMS::asaig-reg_N3_end-of-contract
    "When the delivery has been fulfilled, an end of contract is reached.
    	Fulf(delivery) --> end"
    (asaig-reg (id ?ctx))
    ?ff <- (fulfillment (context ?ctx) (obl ?obl_del) (when ?w) (processing-context INSTITUTIONAL-NORMS))
    ?obl_del <- (obligation (context ?ctx) (fact delivery ref ?ctx $?))
    (not (norm-fired-on (ires ?ff) (context ~INSTITUTIONAL_NORMS)) )
    =>
    (assert (end-context (context ?ctx) (when ?w)))
    (assert (norm-fired-on (ires ?ff) (context INSTITUTIONAL-NORMS))) )


;;;;;;;;;;;;;;;;;;;;;;;;
;; As simple as it gets plus contract

; context definition

(deftemplate MAIN::asaig-plus extends contract
    "A contract with a single obligation to supply the product, plus an associated sanction" )

; contextual-info definitions

(deftemplate MAIN::asaig-plus-data extends contextual-info
    "Data asaig-plus"
    (slot seller)
    (slot buyer)
    (slot product)
    (slot quantity (default 1))
    (slot unit-price (default 0.0))
    (slot delivery-rel-deadline (default 5000))
    (slot delivery-dviol-sanction) )

; default rules

(defrule INSTITUTIONAL-NORMS::asaig-plus_N1_start
    "The contract starts with the seller obligation to deliver the product.
    	start --> Obl(delivery)"
    (asaig-plus (id ?ctx))
    (asaig-plus-data (context ?ctx) (seller ?s) (buyer ?b) (product ?p) (quantity ?q)
        (delivery-rel-deadline ?drd) )
    ?st <- (start-context (context ?ctx) (when ?w) (processing-context INSTITUTIONAL-NORMS))
    (not (norm-fired-on (ires ?st) (context ~INSTITUTIONAL-NORMS)))
    =>
    (assert (obligation (id delivery) (context ?ctx) (bearer ?s) (counterparty ?b) (deadline (+ ?w 4000));?drd))
                (fact delivery ref ?ctx from ?s to ?b product ?p quantity ?q) ) )
    (assert (norm-fired-on (ires ?st) (context INSTITUTIONAL-NORMS))) )

(defrule INSTITUTIONAL-NORMS::asaig-plus_N2_delivery-dviol
    "A deadline violation on the delivery due leads to a sanction being imposed on the seller.
    	DViol(delivery) --> Obl(pay_sanction)"
    (asaig-plus (id ?ctx))
    (asaig-plus-data (context ?ctx) (seller ?s) (buyer ?b) (quantity ?qt) (unit-price ?upr) (delivery-dviol-sanction ?sanction))
    ?dviol <- (deadline-violation (context ?ctx) (obl ?obl) (when ?w) (processing-context INSTITUTIONAL-NORMS))
    ?obl <- (obligation (context ?ctx) (bearer ?s) (counterparty ?b)
                (fact delivery $?) )
    (not (norm-fired-on (ires ?viol) (context ~INSTITUTIONAL-NORMS)))
    =>
    (assert (obligation (id del_dviol_sanction) (context ?ctx) (bearer ?s) (counterparty ?b) (deadline (+ ?w 4000))
                (fact payment ref sanction from ?s to ?b amount (* ?sanction (* ?qt ?upr))) ) )
    (assert (norm-fired-on (ires ?dviol) (context INSTITUTIONAL-NORMS))) )

(defrule INSTITUTIONAL-NORMS::asaig-plus_N3_no-delivery-1
    "A delivery violation leads to end of contract.
    	Viol(delivery), Viol(pay_sanction) --> end"
    (asaig-plus (id ?ctx))
    ?viol <- (violation (context ?ctx) (obl ?obl) (when ?w) (processing-context ?pc_1))
    ?obl <- (obligation (context ?ctx) (fact delivery $?))
    ?viol+ <- (violation (context ?ctx) (obl ?obl+) (when ?w+) (processing-context ?pc_2))
    ?obl+ <- (obligation (context ?ctx) (fact payment $?))
    (test (member$ INSTITUTIONAL-NORMS (list ?pc_1 ?pc_2)))
    (not (norm-fired-on (ires $?ires&:(equal-sets? ?ires (list ?viol ?viol+)))
             (context ~INSTITUTIONAL_NORMS)) )
    =>
    (assert (end-context (context ?ctx) (when (max ?w ?w+))))
    (assert (norm-fired-on (ires ?viol ?viol+) (context INSTITUTIONAL-NORMS))) )

(defrule INSTITUTIONAL-NORMS::asaig-plus_N4_no-delivery-2
    "A delivery violation leads to end of contract.
    	Viol(delivery), Fulf(pay_sanction) --> end"
    (asaig-plus (id ?ctx))
    ?viol <- (violation (context ?ctx) (obl ?obl) (when ?w) (processing-context ?pc_1))
    ?obl <- (obligation (context ?ctx) (fact delivery $?))
    ?fulf+ <- (fulfillment (context ?ctx) (obl ?obl+) (when ?w+) (processing-context ?pc_2))
    ?obl+ <- (obligation (context ?ctx) (fact payment $?))
    (test (member$ INSTITUTIONAL-NORMS (list ?pc_1 ?pc_2)))
    (not (norm-fired-on (ires $?ires&:(equal-sets? ?ires (list ?viol ?fulf+)))
             (context ~INSTITUTIONAL_NORMS)) )
    =>
    (assert (end-context (context ?ctx) (when (max ?w ?w+))))
    (assert (norm-fired-on (ires ?viol ?fulf+) (context INSTITUTIONAL-NORMS))) )

(defrule INSTITUTIONAL-NORMS::asaig-plus_N5_delivery
    "When the delivery has been fulfilled, an end of contract is reached.
    	Fulf(delivery) --> end"
    (asaig-plus (id ?ctx))
    ?fulf <- (fulfillment (context ?ctx) (obl ?obl_del) (when ?w) (processing-context INSTITUTIONAL-NORMS))
    ?obl_del <- (obligation (context ?ctx) (fact delivery ref ?ctx $?))
    (not (deadline-violation (context ?ctx) (obl ?obl-del)))
;    (not (obligation (context ?ctx) (fact payment $?)))
    (not (norm-fired-on (ires ?fulf) (context ~INSTITUTIONAL_NORMS)))
    =>
    (assert (end-context (context ?ctx) (when ?w)))
    (assert (norm-fired-on (ires ?fulf) (context INSTITUTIONAL-NORMS))) )

(defrule INSTITUTIONAL-NORMS::asaig-plus_N6_delivery-1
    "When the delivery has been fulfilled, an end of contract is reached.
    	Fulf(delivery), Fulf(pay_sanction) --> end"
    (asaig-plus (id ?ctx))
    ?fulf <- (fulfillment (context ?ctx) (obl ?obl_del) (when ?w) (processing-context ?pc_1))
    ?obl_del <- (obligation (context ?ctx) (fact delivery ref ?ctx $?))
    ?fulf+ <- (fulfillment (context ?ctx) (obl ?obl+) (when ?w+) (processing-context ?pc_2))
    ?obl+ <- (obligation (context ?ctx) (fact payment $?))
    (test (member$ INSTITUTIONAL-NORMS (list ?pc_1 ?pc_2)))
    (not (norm-fired-on (ires $?ires&:(equal-sets? ?ires (list ?fulf ?fulf+)))
             (context ~INSTITUTIONAL_NORMS)) )
    =>
    (assert (end-context (context ?ctx) (when (max ?w ?w+))))
    (assert (norm-fired-on (ires ?fulf ?fulf+) (context INSTITUTIONAL-NORMS))) )

(defrule INSTITUTIONAL-NORMS::asaig-plus_N7_delivery-2
    "When the delivery has been fulfilled, an end of contract is reached.
    	Fulf(delivery), Viol(pay_sanction) --> end"
    (asaig-plus (id ?ctx))
    ?fulf <- (fulfillment (context ?ctx) (obl ?obl_del) (when ?w) (processing-context ?pc_1))
    ?obl_del <- (obligation (context ?ctx) (fact delivery ref ?ctx $?))
    ?viol+ <- (violation (context ?ctx) (obl ?obl+) (when ?w+) (processing-context ?pc_2))
    ?obl+ <- (obligation (context ?ctx) (fact payment $?))
    (test (member$ INSTITUTIONAL-NORMS (list ?pc_1 ?pc_2)))
    (not (norm-fired-on (ires $?ires&:(equal-sets? ?ires (list ?fulf ?viol+)))
             (context ~INSTITUTIONAL_NORMS)) )
    =>
    (assert (end-context (context ?ctx) (when (max ?w ?w+))))
    (assert (norm-fired-on (ires ?fulf ?viol+) (context INSTITUTIONAL-NORMS))) )


;;;;;;;;;;;;;;;;;;;;;;;;
;; Contract of sale

; context definition

(deftemplate MAIN::contract-of-sale extends contract
    "A contract of sale between a seller and a buyer" )

; contextual-info definitions

(deftemplate MAIN::contract-of-sale-data extends contextual-info
    "Data for contract of sale"
    (slot seller)
    (slot buyer)
    (slot product)
    (slot quantity (default 1))
    (slot unit-price (default 0))
    (slot delivery-rel-liveline (default 0))
    (slot delivery-rel-deadline (default 5000))
    (slot payment-rel-deadline (default 30000)) )

; default rules

(defrule INSTITUTIONAL-NORMS::CoS_N1_delivery
    "The contract starts with the seller obligation to deliver the product.
    	start --> Obl(delivery)"
    (contract-of-sale (id ?ctx))
    (contract-of-sale-data (context ?ctx) (seller ?s) (buyer ?b) (product ?p) (quantity ?q)
        (delivery-rel-liveline ?drl) (delivery-rel-deadline ?drd) )
    ?st <- (start-context (context ?ctx) (when ?w) (processing-context INSTITUTIONAL-NORMS))
    (not (norm-fired-on (ires ?st) (context ~INSTITUTIONAL-NORMS)))
    =>
    (assert (obligation (id delivery) (context ?ctx) (bearer ?s) (counterparty ?b) (liveline (+ ?w ?drl)) (deadline (+ ?w ?drd))
                (fact delivery ref ?ctx from ?s to ?b product ?p quantity ?q) ) )
    (assert (norm-fired-on (ires ?st) (context INSTITUTIONAL-NORMS))) )

(defrule INSTITUTIONAL-NORMS::CoS_N2_payment
    "After delivery has been made, the buyer will be obliged to pay the agreed upon amount.
    	Fulf(delivery) --> Obl(payment)"
    (contract-of-sale (id ?ctx))
    (contract-of-sale-data (context ?ctx) (seller ?s) (buyer ?b) (product ?p) (quantity ?q) (unit-price ?up)
        (payment-rel-deadline ?prd) )
    ?ff <- (fulfillment (context ?ctx) (obl ?obl) (when ?w) (processing-context INSTITUTIONAL-NORMS))
    ?obl <- (obligation (context ?ctx) (bearer ?s) (counterparty ?b) (fact delivery $? product ?p $?))
    (not (norm-fired-on (ires ?ff) (context ~INSTITUTIONAL-NORMS)))
    =>
    (assert (obligation (id payment) (context ?ctx) (bearer ?b) (counterparty ?s) (deadline (+ ?w ?prd))
                (fact payment ref ?ctx from ?b to ?s amount (* ?q ?up)) ) )
    (assert (norm-fired-on (ires ?ff) (context INSTITUTIONAL-NORMS))) )

(defrule INSTITUTIONAL-NORMS::CoS_N3_no-delivery
    "A delivery violation leads to end of contract.
    	Viol(delivery) --> end"
    (contract-of-sale (id ?ctx))
    ?viol <- (violation (context ?ctx) (obl ?obl) (when ?w) (processing-context INSTITUTIONAL-NORMS))
    ?obl <- (obligation (context ?ctx) (fact delivery $?) )
    (not (norm-fired-on (ires ?viol) (context ~INSTITUTIONAL-NORMS)))
    =>
    (assert (end-context (context ?ctx) (when ?w)))
    (assert (norm-fired-on (ires ?viol) (context INSTITUTIONAL-NORMS))) )

(defrule INSTITUTIONAL-NORMS::CoS_N4_no-payment
    "A deadline violation on the payment due leads to an interest being imposed on the buyer.
    	DViol(payment) --> Obl(pay_interest)"
    (contract-of-sale (id ?ctx))
    (contract-of-sale-data (context ?ctx) (seller ?s) (buyer ?b)
        (payment-rel-deadline ?prd) )
    ?dviol <- (deadline-violation (context ?ctx) (obl ?obl) (when ?w) (processing-context INSTITUTIONAL-NORMS))
    ?obl <- (obligation (context ?ctx) (bearer ?b) (counterparty ?s)
                (fact payment ref ?ctx $? amount ?a) )
    (not (norm-fired-on (ires ?dviol) (context ~INSTITUTIONAL-NORMS)))
    =>
    (assert (obligation (id pay_dviol_interest) (context ?ctx) (bearer ?b) (counterparty ?s) (deadline (+ ?w ?prd))
                (fact payment ref interest from ?b to ?s amount (* ?a 0.1)) ) )
    (assert (norm-fired-on (ires ?dviol) (context INSTITUTIONAL-NORMS))) )

; BUG in Jess prevents this norm from working
(defrule INSTITUTIONAL-NORMS::CoS_N5_end-of-contract
    "When both delivery and payment have been fulfilled, an end of contract is reached.
    	Fulf(delivery), Fulf(payment) --> end"		; and provided there are no unpayed interests
    (contract-of-sale (id ?ctx))
    ?ff_del <- (fulfillment (context ?ctx) (obl ?obl_del) (when ?w_del) (processing-context ?pc_1))
    ?obl_del <- (obligation (context ?ctx) (fact delivery ref ?ctx $?))
    ?ff_pay <- (fulfillment (context ?ctx) (obl ?obl_pay) (when ?w_pay) (processing-context ?pc_2))
    ?obl_pay <- (obligation (context ?ctx) (fact payment ref ?ref $?))
    (not (and ?obl1 <- (obligation (context ?ctx) (fact payment $?))   ; no pending deliveries (for the special case of multiple products)
              (not (or (fulfillment (context ?ctx) (obl ?obl1))
                       (violation (context ?ctx) (obl ?obl1)) ) ) ) )   ; Jess bug (see e-mails)
    (not (and ?obl2 <- (obligation (context ?ctx) (fact payment $?))   ; no pending payments
              (not (or (fulfillment (context ?ctx) (obl ?obl2))
                       (violation (context ?ctx) (obl ?obl2)) ) ) ) )   ; Jess bug (see e-mails)
    (test (member$ INSTITUTIONAL-NORMS (list ?pc_1 ?pc_2)))
    (not (norm-fired-on (ires $?ires&:(equal-sets? ?ires (list ?ff_del ?ff_pay)))
             (context ~INSTITUTIONAL_NORMS)) )
    =>
    (assert (end-context (context ?ctx) (when (max ?w_del ?w_pay))))
    (assert (norm-fired-on (ires ?ff_del ?ff_pay) (context INSTITUTIONAL-NORMS))) )

; deffunction create-contract-of-sale
(deffunction create-contract-of-sale ($?args)
    ; module
    (bind ?id (sym-cat CoS-T (random)))
    (build (str-cat "(defmodule " ?id " (declare (auto-focus TRUE)))"))
    ; background information
    (assert (contract-of-sale (id ?id) (super-context (nth$ 2 $?args)) (when (nth$ 4 $?args)) (who (nth$ 6 $?args) (nth$ 8 $?args)) ) )
    (assert (contract-of-sale-data (context ?id) (seller (nth$ 6 $?args)) (buyer (nth$ 8 $?args))
                (product (nth$ 10 $?args)) (quantity (nth$ 12 $?args)) (unit-price (nth$ 14 $?args)) ) ) )


;;;;;;;;;;;;;;;;;;;;;;;;
;; Supply agreement   (*** COIN@AAMAS'08 / ECAI'08 ***) --> also in the Thesis

; context definition

(deftemplate MAIN::supply-agreement extends contract
    "A supply-agreement where partners promise to supply products when asked for, without task precedence (delivery and payment)" )

; contextual-info definitions

(deftemplate MAIN::supply-info extends contextual-info
    "Cooperation effort of a CA partner"
    (slot agent)
    (slot product)
    (slot unit-price (default 0.0)) )

; default rules

(defrule INSTITUTIONAL-NORMS::SA_order-delivery-payment
    "Whenever a partner orders a specific product to another partner, the latter shall be obliged to deliver, and the former to pay.
    	IFact(order) --> Obl(delivery), Obl(payment)"
    (supply-agreement (id ?ctx))
    (supply-info (context ?ctx) (agent ?ag2) (product ?p) (unit-price ?upr))
    ?ifa <- (ifact (context ?ctx) (when ?w) (processing-context INSTITUTIONAL-NORMS)
                (fact order ref ?ref from ?ag1 to ?ag2 product ?p quantity ?qt) )
    (not (norm-fired-on (ires ?ifa) (context ~INSTITUTIONAL-NORMS)))
    =>
    (printout t "---N1^top fired on " ?ifa crlf)
    (assert (obligation (id delivery) (context ?ctx) (bearer ?ag2) (counterparty ?ag1) (deadline (+ ?w 2000))
                (fact delivery ref ?ref from ?ag2 to ?ag1 product ?p quantity ?qt) ) )
    (assert (obligation (id payment) (context ?ctx) (bearer ?ag1) (counterparty ?ag2) (deadline (+ ?w 2000))
                (fact payment ref ?ref from ?ag1 to ?ag2 amount (* ?qt ?upr)) ) )
    (assert (norm-fired-on (ires ?ifa) (context INSTITUTIONAL-NORMS))) )


;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Cluster
;
;; context definition
;
;(deftemplate MAIN::cluster extends contract
;    "A cluster of acquainted enterprises" )
;
;; contextual-info definitions
;
;(deftemplate MAIN::acquainted-info extends contextual-info
;    "International Standard Industrial Classification for each agent"
;    (slot agent)
;    (slot isic-code) )
;
;; default rules
;
;(defrule INSTITUTIONAL-NORMS::cluster_cfp-prop
;    "A cfp demands for a proposal within a reasonable time"
;    (cluster (id ?ctx))
;    (acquainted-info (context ?ctx) (agent ?ag1) (isic-code ?isic))
;    ?ifa <- (ifact (context ?ctx) (when ?w) (processing-context INSTITUTIONAL-NORMS)
;                (fact cfp ref ?ref from ?ag to ?ag1 isic-code ?isic $?) )
;    (not (norm-fired-on (ires ?ifa) (context ~INSTITUTIONAL-NORMS)))
;    =>
;    (assert (obligation (id proposal) (context ?ctx) (bearer ?ag1) (counterparty ?ag) (deadline (+ ?w 10000))
;                (fact proposal ref ?ref from ?ag1 to ?ag isic-code ?isic $?) ) )
;    (assert (norm-fired-on (ires ?ifa) (context INSTITUTIONAL-NORMS))) )


;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Workflow contract (*** abstracted from multi-party contract in Lai Xu's thesis ***)
;
;; context definition
;
;(deftemplate MAIN::workflow-contract extends contract
;    "A workflow contract, based on the exchange of messages that flow among participants" )
;
;; contextual-info definitions
;
;; Defining appropriate structures to capture an arbitrary workflow:
;; - actions: name, sender, receiver, relative deadline
;; - constraints: action, action - precedence relationships between pairs of actions
;; Overall note: all actions are considered as msg-delivery's (in terms of obligations and fulfillments).
;
;(deftemplate MAIN::action extends contextual-info
;    "An action to be taken by a workflow-contract participant"
;    (slot name)   ; an action identifier
;    (slot sender)   ; who should execute this action (e.g. send a document)
;    (slot receiver)   ; who should receive the output of this action (e.g. a document)
;    (slot rel-deadline (default 0)) )   ; relative deadline
;
;(deftemplate MAIN::constraint extends contextual-info
;    "A constraint on the order of two actions"
;    (slot action1)
;    (slot action2) )
;
;; default rules
;
;; WfC v1: version with 1 norm
;; This approach includes a norm that does not rely on fulfillments, but instead on constraints that order the actions.
;; A potential problem is that if an agent executes an intermediate action, this may trigger only a part of the workflow...
;
;;(defrule INSTITUTIONAL-NORMS::WfC-actions
;;    "Dealing with actions"
;;    (workflow-contract (id ?ctx))
;;    (action (name ?action) (sender ?fr) (receiver ?to))
;;    ; get a next-action depending on this one
;;    (constraint (action1 ?action) (action2 ?next-action))
;;    (action (name ?next-action) (sender ?s) (receiver ?r) (rel-deadline ?d))
;;    ; when the action is executed
;;    ?ifa <- (ifact (context ?ctx) (when ?w) (processing-context INSTITUTIONAL-NORMS)
;;                (fact ?action ref ?ref from ?fr to ?to $?) )
;;    ; check that no precedence is pending for next-action
;;    (not (and (constraint (action1 ?previous-action) (action2 ?next-action))
;;              (test (neq ?previous-action ?action))
;;              (action (name ?previous-action) (sender ?s1) (receiver ?r1))
;;              (not (ifact (context ?ctx) (fact ?previous-action ref ? from ?s1 to ?r1 $?))) ) )
;;    (not (norm-fired-on (ires ?ifa) (context ~INSTITUTIONAL-NORMS)))
;;    =>
;;;    (printout t "~~~~~> " ?action " - " ?next-action crlf)
;;    (assert (obligation (context ?ctx) (bearer ?s) (counterparty ?r) (deadline (+ ?w ?d))
;;                (fact msg-delivery ref ?ref from ?s to ?r msg ?next-action) ) )
;;    (assert (norm-fired-on (ires ?ifa) (context INSTITUTIONAL-NORMS))) )
;
;; WfC v2: version with 2 norms
;; In this approach, I consider two kinds of actions: starting and obliged. Starting actions (there is typically only one) start the workflow
;; process, and therefore were not obliged by any norm (similar to an order being placed). Obliged actions are the ones that are constrained
;; by other actions in the workflow definition, and therefore will be obliged once their preceeding actions have been executed.
;
;(defrule INSTITUTIONAL-NORMS::WfC-starting-actions
;    "Dealing with the starting action (not constrained by no other, and therefore not obliged)"
;    (workflow-contract (id ?ctx))
;    (action (name ?action) (sender ?fr) (receiver ?to))
;    ; check that this is a starting action action (i.e. not constrained by another one; if it is, then it is handled by norm 2 below)
;    ; (another possibility would be to check that there is no associated obligation)
;    (not (constraint (action2 ?action)))
;    ; get a next-action depending on this one
;    (constraint (action1 ?action) (action2 ?next-action))
;    (action (name ?next-action) (sender ?s) (receiver ?r) (rel-deadline ?d))
;    ; when the action is executed
;    ?ifa <- (ifact (context ?ctx) (when ?w) (processing-context INSTITUTIONAL-NORMS)
;                (fact ?action ref ?ref from ?fr to ?to $?) )
;;    ; check that no precedence is pending for next-action (assuming that there can be more than one starting action)
;;    (not (and (constraint (action1 ?previous-action) (action2 ?next-action))
;;              (test (neq ?previous-action ?action))
;;              (action (name ?previous-action) (sender ?s1) (receiver ?r1))
;;              (not (ifact (context ?ctx) (fact ?previous-action ref ? from ?s1 to ?r1 $?))) ) )
;    (not (norm-fired-on (ires ?ifa) (context ~INSTITUTIONAL-NORMS)))
;    =>
;;    (printout t "*****> " ?action " - " ?next-action crlf)
;    (assert (obligation (context ?ctx) (bearer ?s) (counterparty ?r) (deadline (+ ?w ?d))
;                (fact msg-delivery ref ?ref from ?s to ?r msg ?next-action) ) )
;    (assert (norm-fired-on (ires ?ifa) (context INSTITUTIONAL-NORMS))) )
;
;(defrule INSTITUTIONAL-NORMS::WfC-obliged-actions
;    "Dealing with obliged actions resulting from the underlying workflow"
;    (workflow-contract (id ?ctx))
;    (action (name ?action) (sender ?fr) (receiver ?to))
;    ; get a next-action depending on this one
;    (constraint (action1 ?action) (action2 ?next-action))
;    (action (name ?next-action) (sender ?s) (receiver ?r) (rel-deadline ?d))
;    ; when the action is fulfilled
;    ?ff <- (fulfillment (context ?ctx) (when ?w) (obl ?obl) (processing-context INSTITUTIONAL-NORMS))
;    ?obl <- (obligation (context ?ctx) (fact msg-delivery ref ?ref from ?fr to ?to msg ?action $?))
;    ; check that no precedence is pending for next-action (the fact that this test relies on fulfillments prevents an action from depending on both obliged and starting actions)
;    (not (and (constraint (action1 ?previous-action) (action2 ?next-action))
;              (test (neq ?previous-action ?action))
;              (action (name ?previous-action) (sender ?s1) (receiver ?r1))
;;              (not (and (fulfillment (context ?ctx) (obl ?obl_))   ; Jess bug
;;                        ?obl_ <- (obligation (context ?ctx) (fact msg-delivery ref ?ref from ?s1 to ?r1 msg ?previous-action $?)) ) ) ) )
;              (not (ifact (context ?ctx) (fact ?previous-action ref ? from ?s1 to ?r1 $?))) ) )   ; because of Jess bug
;    (not (norm-fired-on (ires ?ff) (context ~INSTITUTIONAL-NORMS)))
;    =>
;;    (printout t "~~~~~> " ?action " - " ?next-action crlf)
;    (assert (obligation (context ?ctx) (bearer ?s) (counterparty ?r) (deadline (+ ?w ?d))
;                (fact msg-delivery ref ?ref from ?s to ?r msg ?next-action) ) )
;    (assert (norm-fired-on (ires ?ff) (context INSTITUTIONAL-NORMS))) )


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; MODULES (CONTRACTS) FOR TESTING PURPOSES
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;(call java.lang.Thread sleep 5000)   ; trick to let all agents setup before putting testing contracts in practice

;;;;;;;;;;;;;;;;;;;;;;;;
;; A typical contract of sale: CoS-T12

; module

;(defmodule CoS-T12
;    "A typical contract of sale"
;    (declare (auto-focus TRUE)) )

; background information

;(deffacts CoS-T12-facts
;	"Contract-of-sale data"
;    
;    (contract-of-sale
;        (id CoS-T12)
;        (when (+ (call System currentTimeMillis) 5000))   ; giving time for agents to setup
;        (who ForOffice LIACC) )
;    
;    (contract-of-sale-data
;        (context CoS-T12)
;        (seller ForOffice) (buyer LIACC)
;        (product computer-desk) (quantity 10) (unit-price 20.0)
;        (delivery-rel-liveline 3000) ) )


;;;;;;;;;;;;;;;;;;;;;;;;
;; An atypical contract of sale: CoS-A12

; module

;(defmodule CoS-A12
;    "An atypical contract of sale"
;    (declare (auto-focus TRUE)) )

; background information

;(deffacts CoS-A12-facts
;	"Contract-of-sale background information"
;    
;    (contract-of-sale
;        (id CoS-A12)
;        (when (+ (call System currentTimeMillis) 5000))   ; giving time for agents to setup
;        (who CPUStore LIACC) )
;    
;    (contract-of-sale-data
;        (context CoS-A12)
;        (seller CPUStore) (buyer LIACC)
;        (product laptop-computer) (quantity 1) (unit-price 800.0) ) )

; specific norms

;(defrule CoS-A12::void
;    "Nothing after delivery"
;    (contract-of-sale (id CoS-A12))
;    ?ff <- (fulfillment (context CoS-A12) (obl ?obl) (when ?w) (processing-context CoS-A12))
;    ?obl <- (obligation (context CoS-A12) (bearer ?s) (counterparty ?b) (fact delivery $?))
;    (not (norm-fired-on (ires ?ff) (context ~CoS-A12)))
;    =>
;    (assert (norm-fired-on (ires ?ff) (context CoS-A12))) )
;
;(defrule CoS-A12::payment
;    "After delivery and no return within period, buyer obligation to pay"
;    (contract-of-sale (id CoS-A12))
;    (contract-of-sale-data (context CoS-A12) (seller ?s) (buyer ?b) (product ?p) (quantity ?q) (unit-price ?up)
;        (payment-rel-deadline ?prd) )
;    ?ff <- (fulfillment (context CoS-A12) (obl ?obl) (when ?w) (processing-context ?pc_1))
;    ?obl <- (obligation (context CoS-A12) (bearer ?s) (counterparty ?b) (fact delivery $?))
;    (time-scheduler :(schedule-time-alert CoS-A12 (+ ?w 10000)))
;    ?time <- (time (context CoS-A12) (when =(+ ?w 10000)) (processing-context ?pc_2))
;    (not (ifact (context CoS-A12) (fact delivery ref ? from ?b to ?s product ?p quantity ?q) {when <= (+ ?w 10000)}))
;    (test (member$ CoS-A12 (list ?pc_1 ?pc_2)))
;    (not (norm-fired-on (ires $?ires&:(equal-sets? ?ires (list ?ff ?time))) (context ~CoS-A12)))
;    =>
;    (assert (obligation (context CoS-A12) (bearer ?b) (counterparty ?s) (deadline (+ ?w ?prd))
;                (fact payment ref CoS-A12 from ?b to ?s amount (* ?q ?up)) ) )
;    (assert (norm-fired-on (ires ?ff ?time) (context CoS-A12))) )
;
;(defrule CoS-A12::end-of-contract
;    "Delivery and product return lead to end of contract"
;    (contract-of-sale (id CoS-A12))
;    (contract-of-sale-data (context CoS-A12) (seller ?s) (buyer ?b) (product ?p) (quantity ?q) (unit-price ?up)
;        (payment-rel-deadline ?prd) )
;    ?ff_del <- (fulfillment (context CoS-A12) (obl ?obl_del) (processing-context ?pc_1))
;    ?obl_del <- (obligation (context CoS-A12) (bearer ?s) (counterparty ?b) (fact delivery ref ? from ?s to ?b product ?p quantity ?q))
;    ?ifa_ret <- (ifact (context ?CoS-A12) (fact delivery ref ? from ?b to ?s product ?p quantity ?q) (when ?w_ret) (processing-context ?pc_2))
;    (test (member$ CoS-A12 (list ?pc_1 ?pc_2)))
;    (not (norm-fired-on (ires $?ires&:(equal-sets? ?ires (list ?ff_del ?ifa_ret))) (context ~CoS-A12)))
;    =>
;    (assert (end-context (context CoS-A12) (when ?w_ret)))
;    (assert (norm-fired-on (ires ?ff_del ?ifa_ret) (context CoS-A12))) )


;;;;;;;;;;;;;;;;;;;;;;;;
;; Supply-agreement: SA3   (*** COIN@AAMAS'08 / ECAI'08 ***) --> also in the Thesis

; module

;(defmodule SA3
;    "A supply agreement between jim, sam and tom"
;    (declare (auto-focus TRUE)) )

; background information

;(deffacts SA3-facts
;    "Supply-agreement background information"
;    
;    (supply-agreement
;        (id SA3)
;        (when (+ (call System currentTimeMillis) 5000))
;        (who jim sam tom) )
;    
;    (supply-info (context SA3) (agent jim) (product p1) (unit-price 1.0))
;    (supply-info (context SA3) (agent sam) (product p2) (unit-price 1.0))
;    (supply-info (context SA3) (agent tom) (product p3) (unit-price 1.0)) )

; specific norms

;(defrule SA3::N1
;    "jim has an extended delivery deadline for orders with more than 99 units"
;    (supply-agreement (id SA3))
;    (supply-info (context SA3) (agent jim) (product ?p) (unit-price ?upr))
;    ?ifa <- (ifact (context SA3) (when ?w) (processing-context SA3)
;                (fact order ref ?ref from ?ag1 to jim product ?p quantity ?qt) )
;    (not (norm-fired-on (ires ?ifa) (context ~SA3)))
;    (test (> ?qt 99))
;    =>
;    (printout t "---N1^SA3 fired on " ?ifa crlf)
;    (assert (obligation (context SA3) (bearer jim) (counterparty ?ag1) (deadline (+ ?w 5000))
;                (fact delivery ref ?ref from jim to ?ag1 product ?p quantity ?qt) ) )
;    (assert (obligation (context SA3) (bearer ?ag1) (counterparty jim) (deadline (+ ?w 2000))
;                (fact payment ref ?ref from ?ag1 to jim amount (* ?qt ?upr)) ) )
;    (assert (norm-fired-on (ires ?ifa) (context SA3))) )
;
;(defrule SA3::N2
;    "higher position of sam: first gets the products..."
;    (supply-agreement (id SA3))
;    (supply-info (context SA3) (agent ?ag2) (product ?p))
;    ?ifa <- (ifact (context SA3) (when ?w) (processing-context SA3)
;                (fact order ref ?ref from sam to ?ag2 product ?p quantity ?qt) )
;    (not (norm-fired-on (ires ?ifa) (context ~SA3)))
;    =>
;    (printout t "---N2^SA3 fired on " ?ifa crlf)
;    (assert (obligation (context SA3) (bearer ?ag2) (counterparty sam) (deadline (+ ?w 2000))
;                (fact delivery ref ?ref from ?ag2 to sam product ?p quantity ?qt) ) )
;    (assert (norm-fired-on (ires ?ifa) (context SA3))) )
;
;(defrule SA3::N3
;    "higher position of sam: ...then pays"
;    (supply-agreement (id SA3))
;    (supply-info (context SA3) (agent ?ag2) (product ?p) (unit-price ?upr))
;    ?ff <- (fulfillment (context SA3) (when ?w) (obl ?obl) (processing-context SA3))
;    ?obl <- (obligation (context SA3) (bearer ?ag2) (counterparty sam) (fact delivery ref ?ref from ?ag2 to sam product ?p quantity ?qt))
;    (not (norm-fired-on (ires ?ff) (context ~SA3)))
;    =>
;    (printout t "---N3^SA3 fired on " ?ff crlf)
;    (assert (obligation (context SA3) (bearer sam) (counterparty ?obl.bearer) (deadline (+ ?w 2000))
;                (fact payment ref ?ref from sam to ?ag2 amount (* ?qt ?upr)) ) )
;    (assert (norm-fired-on (ires ?ff) (context SA3))) )

;; To test, copy&paste to the normative environment's GUI, followed by (run):
;; (assert (ifact (context SA3) (fact order ref 1 from jim to tom product p3 quantity 5) ) )
;; (assert (ifact (context SA3) (fact order ref 2 from tom to jim product p1 quantity 5) ) )
;; (assert (ifact (context SA3) (fact order ref 3 from tom to jim product p1 quantity 100) ) )
;; (assert (ifact (context SA3) (fact order ref 4 from sam to tom product p3 quantity 5) ) )


;;;;;;;;;;;;;;;;;;;;;;;;
;; A specific cluster: Cluster-12345

; module

;(defmodule Cluster-12345
;    "A cluster including several acquainted companies"
;    (declare (auto-focus TRUE)) )

; background information

;(deffacts Cluster-12345-facts
;    "Cluster-12345 background information"
;    
;    (cluster
;        (id Cluster-12345)
;        (when (+ (call System currentTimeMillis) 5000))
;        (who E1 E2 E3 E4 E5) )
;    
;    (acquainted-info (context Cluster-12345) (agent E1) (isic-code 261)) ; 261 - Manufacture of electronic components and boards
;    (acquainted-info (context Cluster-12345) (agent E2) (isic-code 261))
;    (acquainted-info (context Cluster-12345) (agent E3) (isic-code 262)) ; 262 - Manufacture of computers and peripheral equipment
;    (acquainted-info (context Cluster-12345) (agent E4) (isic-code 262))
;    (acquainted-info (context Cluster-12345) (agent E5) (isic-code 263)) ; 263 - Manufacture of communication equipment
;    )

; norms for coop-agreement subcontexts

;; Cluster-12345 :: Cooperation agreement

; context definition

;(deftemplate Cluster-12345::coop-agreement extends contract
;    "A cooperation agreement where parties promise to supply products within agreed criteria" )

; contextual-info definitions

;(deftemplate Cluster-12345::coop-effort extends contextual-info
;    "Cooperation effort of a coop-agreement partner"
;    (slot agent)
;    (slot product)
;    (slot min-qt (default 1))
;    (slot max-qt (default 1))
;    (slot unit-price (default 0.0)) )

; default rules

;(defrule Cluster-12345::CA_order-accept
;    "A partner must accept orders conforming to his coop-effort promise"
;    (coop-agreement (id ?ctx) (who $?who))
;    (coop-effort (context ?ctx) (agent ?ag1) (product ?p) (min-qt ?min_q) (max-qt ?max_q))
;    ?ifa <- (ifact (context ?ctx) (when ?w) (processing-context Cluster-12345)
;                (fact order ref ?ref from ?ag to ?ag1 product ?p quantity ?qt) )
;    (test (member$ ?ag $?who))
;    (test (and (>= ?qt ?min_q) (<= ?qt ?max_q)))
;    (not (norm-fired-on (ires ?ifa) (context ~Cluster-12345)))
;    =>
;    (assert (obligation (context ?ctx) (bearer ?ag1) (counterparty ?ag) (deadline (+ ?w 5000))
;                (fact msg-delivery ref ?ref from ?ag1 to ?ag msg accept product ?p quantity ?qt) ) )
;;                (fact accept ref ?ref from ?ag1 to ?ag product ?p quantity ?qt) ) )
;    (assert (norm-fired-on (ires ?ifa) (context Cluster-12345))) )
;
;(defrule Cluster-12345::CA_accept-CoS
;    "Accepting an order means to establish a new contract-of-sale"
;    (coop-agreement (id ?ctx))
;    (coop-effort (context ?ctx) (agent ?ag1) (product ?p) (unit-price ?upr))
;    ?ifa_order <- (ifact (context ?ctx) (when ?w_order) (processing-context ?pc_1)
;                      (fact order ref ?ref from ?ag to ?ag1 product ?p quantity ?qt) )
;    ?ifa_accept <- (ifact (context ?ctx) (when ?w_accept) (processing-context ?pc_2)
;                      (fact accept ref ?ref from ?ag1 to ?ag product ?p quantity ?qt) )
;    (test (member$ Cluster-12345 (list ?pc_1 ?pc_2)))
;    (not (norm-fired-on (ires $?ires&:(equal-sets? ?ires (list ?ifa_order ?ifa_accept))) (context ~Cluster-12345)))
;    =>
;    ; establish contract-of-sale
;    (create-contract-of-sale super-context ?ctx when ?w_accept seller ?ag1 buyer ?ag product ?p quantity ?qt unit-price ?upr)
;    (assert (norm-fired-on (ires ?ifa_order ?ifa_accept) (context Cluster-12345))) )

;;;;;;;;;;;;
;; Coop-agreement: CA-123

; module

;(defmodule CA-123
;    "A cooperation agreement between 3 companies"
;    (declare (auto-focus TRUE)) )

; background information

;(deffacts CA-123-facts
;    "CA-123 background information"
;    
;    (Cluster-12345::coop-agreement
;        (id CA-123)
;        (super-context Cluster-12345)
;        (when (+ (call System currentTimeMillis) 5000))
;        (who E1 E3 E5) )
;    
;    (Cluster-12345::coop-effort (context CA-123) (agent E1) (product motherboard) (min-qt 90) (max-qt 100) (unit-price 40.0))
;    (Cluster-12345::coop-effort (context CA-123) (agent E2) (product desktop-computer) (min-qt 90) (max-qt 100) (unit-price 500.0))
;    (Cluster-12345::coop-effort (context CA-123) (agent E2) (product monitor) (min-qt 90) (max-qt 100) (unit-price 150.0))
;    (Cluster-12345::coop-effort (context CA-123) (agent E3) (product modem) (min-qt 90) (max-qt 100) (unit-price 120.0)) )

;; To test, copy&paste to the normative environment's GUI, followed by (run):
;; (assert (ifact (context CA-123) (fact order ref 1 from E3 to E1 product motherboard quantity 90) ) )


;;;;;;;;;;;;;;;;;;;;;;;;
;; Car insurance   (*** from Lai Xu thesis ***)

; module

;(defmodule WfC-car-insurance
;    "Car insurance"
;    (declare (auto-focus TRUE)) )

; background information

;(deffacts WfC-car-insurance-facts
;    "car-insurance background information"
;    
;    (workflow-contract
;        (id WfC-car-insurance)
;        (when (+ (call System currentTimeMillis) 5000))
;        (who policyholder agfil euro_assist lee_cs garage assessor) )
;    
;    (action (context WfC-car-insurance) (name phoneClaim) (sender policyholder) (receiver euro_assist)) ; no deadline should be needed, because this is the starting action (no obligation)
;    (action (context WfC-car-insurance) (name receiveInfo) (sender policyholder) (receiver euro_assist) (rel-deadline 1000)) ; deadline=0 does not make sense
;    (action (context WfC-car-insurance) (name assignGarage) (sender euro_assist) (receiver policyholder) (rel-deadline 1000)) ; deadline=0 does not make sense
;    (action (context WfC-car-insurance) (name sendCar) (sender policyholder) (receiver garage) (rel-deadline 1000))
;    (action (context WfC-car-insurance) (name estimateRepairCost) (sender garage) (receiver policyholder) (rel-deadline 2000))
;    (action (context WfC-car-insurance) (name notifyClaim) (sender euro_assist) (receiver agfil) (rel-deadline 1000))
;    (action (context WfC-car-insurance) (name forwardClaim) (sender agfil) (receiver lee_cs) (rel-deadline 1000))
;    (action (context WfC-car-insurance) (name contactGarage) (sender lee_cs) (receiver garage) (rel-deadline 1000))
;    (action (context WfC-car-insurance) (name sendClaimForm) (sender agfil) (receiver policyholder) (rel-deadline 2000))
;    (action (context WfC-car-insurance) (name returnClaimForm) (sender policyholder) (receiver agfil) (rel-deadline 7000))
;    (action (context WfC-car-insurance) (name sendRepairCost) (sender garage) (receiver lee_cs) (rel-deadline 1000))
;    (action (context WfC-car-insurance) (name assignAssessor) (sender lee_cs) (receiver assessor) (rel-deadline 1000))
;    (action (context WfC-car-insurance) (name inspectCar) (sender assessor) (receiver lee_cs) (rel-deadline 1000))
;    (action (context WfC-car-insurance) (name sendNewRepairCost) (sender assessor) (receiver lee_cs) (rel-deadline 3000))
;    (action (context WfC-car-insurance) (name agreeRepair) (sender lee_cs) (receiver garage) (rel-deadline 3000))
;    (action (context WfC-car-insurance) (name repairCar) (sender garage) (receiver policyholder) (rel-deadline 5000))
;    (action (context WfC-car-insurance) (name sendInvoice) (sender garage) (receiver lee_cs) (rel-deadline 10000))
;    (action (context WfC-car-insurance) (name forwardInvoice) (sender lee_cs) (receiver agfil) (rel-deadline 6000))
;    (action (context WfC-car-insurance) (name payRepairCost) (sender agfil) (receiver garage) (rel-deadline 30000))
;    
;    (constraint (context WfC-car-insurance) (action1 phoneClaim) (action2 receiveInfo))
;    (constraint (context WfC-car-insurance) (action1 receiveInfo) (action2 assignGarage));
;    (constraint (context WfC-car-insurance) (action1 receiveInfo) (action2 notifyClaim));
;    (constraint (context WfC-car-insurance) (action1 assignGarage) (action2 sendCar));
;    (constraint (context WfC-car-insurance) (action1 sendCar) (action2 estimateRepairCost));
;    (constraint (context WfC-car-insurance) (action1 notifyClaim) (action2 forwardClaim));
;    (constraint (context WfC-car-insurance) (action1 notifyClaim) (action2 sendClaimForm));
;    (constraint (context WfC-car-insurance) (action1 forwardClaim) (action2 contactGarage));
;    (constraint (context WfC-car-insurance) (action1 sendClaimForm) (action2 returnClaimForm));
;    (constraint (context WfC-car-insurance) (action1 estimateRepairCost) (action2 sendRepairCost))
;    (constraint (context WfC-car-insurance) (action1 contactGarage) (action2 sendRepairCost))
;    (constraint (context WfC-car-insurance) (action1 sendRepairCost) (action2 assignAssessor))
;    (constraint (context WfC-car-insurance) (action1 assignAssessor) (action2 inspectCar))
;    (constraint (context WfC-car-insurance) (action1 inspectCar) (action2 sendNewRepairCost))
;    (constraint (context WfC-car-insurance) (action1 sendNewRepairCost) (action2 agreeRepair))
;    (constraint (context WfC-car-insurance) (action1 agreeRepair) (action2 repairCar))
;    (constraint (context WfC-car-insurance) (action1 repairCar) (action2 sendInvoice))
;    (constraint (context WfC-car-insurance) (action1 sendInvoice) (action2 forwardInvoice))
;    (constraint (context WfC-car-insurance) (action1 forwardInvoice) (action2 payRepairCost))
;    (constraint (context WfC-car-insurance) (action1 returnClaimForm) (action2 payRepairCost)) )

;; To test, copy&paste to the normative environment's GUI, followed by (run):
;; (assert (ifact (context WfC-car-insurance) (fact phoneClaim ref 1 from policyholder to euro_assist) ) )


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; RESET
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(reset)

