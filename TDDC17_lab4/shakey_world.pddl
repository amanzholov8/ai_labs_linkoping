(define (domain SHAKEY-WORLD)
	(:requirements 
	        :strips 
	        :equality 
	        :typing )

	(:types
			room 		; A room which can contain things
			shakey 		; The robot
			gripper		; Shakey has 2 grippers to grip objects
			box 		; A box that Shakey can push and use to turn on lights
			object) 	; An object that Shakey can pick up and drop

	(:predicates
		(shakey-at 		?s - shakey ?r - room)		; Shakey is in room r
		(box-at			?b - box ?r - room)			; box b is in room r
		(object-at		?o - object ?r - room)		; object o is in room r
		(adjacent		?r1 ?r2 - room)				; can move from r1 to r2
		(wide-entrance	?r1 ?r2 - room)				; can push a box between r1 and r2
		(has-switch		?r - room)					; room r has a switch and can be lit
		(holding		?o - object ?g - gripper)	; gripper g is holding object o
		(empty			?g - gripper)				; gripper g is not holding anything
		(is-bright			?r - room)				; light in room r is turned on
	)

	(:action move
		:parameters	(?s - shakey ?from ?to - room)

		:precondition 	(and (shakey-at ?s ?from)
							(or (adjacent ?from ?to)
								(adjacent ?to ?from)))

		:effect			(and (shakey-at	?s ?to)
						(not (shakey-at	?s ?from)))
	)

	(:action switch-light-on
		:parameters (?s - shakey ?b - box ?r - room)

		:precondition (and
						(shakey-at ?s ?r)
						(box-at ?b ?r)
						(has-switch ?r)
						(not (is-bright ?r)))

		:effect (is-bright ?r)
	)

	(:action switch-light-off
		:parameters (?s - shakey ?b - box ?r - room)

		:precondition (and
						(shakey-at ?s ?r)
						(box-at ?b ?r)
						(has-switch ?r)
						(is-bright ?r))

		:effect (not (is-bright ?r))
	)

	(:action pick-up
		:parameters (?s - shakey ?o - object ?g - gripper ?r - room)

		:precondition (and
						(shakey-at ?s ?r)
						(object-at ?o ?r)
						(is-bright ?r)
						(empty ?g))

		:effect (and
					(holding ?o ?g)
					(not (empty ?g))
					(not (object-at ?o ?r)))
	)

	(:action put-down
		:parameters (?s - shakey ?o - object ?g - gripper ?r - room)

		:precondition (and
						(shakey-at ?s ?r)
						(holding ?o ?g))

		:effect (and
					(not (holding ?o ?g))
					(empty ?g)
					(object-at ?o ?r))
	)

	(:action push
		:parameters (?s - shakey ?b - box ?from ?to - room)

		:precondition (and
						(box-at ?b ?from)
						(shakey-at ?s ?from)
						(or 
							(wide-entrance ?to ?from) 
							(wide-entrance ?from ?to))
						(or (adjacent ?from ?to)
							(adjacent ?to ?from)))

		:effect (and
					(shakey-at ?s ?to)
					(box-at ?b ?to)
					(not (box-at ?b ?from))
					(not (shakey-at ?s ?from)))
	)
)