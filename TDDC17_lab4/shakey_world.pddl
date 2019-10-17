(define (domain SHAKEY-WORLD)
	(:requirements 
	        :strips 
	        :equality 
	        :typing )

	(:types
			room 		; A room which can contain objects and boxes
			shakey 		; The robot
			gripper		; Shakey has 2 grippers to grip objects
			box 		; A box (Shakey can push around the box and use it to turn on lights)
			object) 	; An object (Shakey can pick up and drop it)

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

	;this action is needed for Shakey to move around
	;Shakey should be in the room "from" and the rooms "from" and "to" should be adjacent
	;In the end Shakey will be in room "to"
	(:action move
		:parameters	(?s - shakey ?from ?to - room)

		:precondition 	(and (shakey-at ?s ?from)
							(or (adjacent ?from ?to)
								(adjacent ?to ?from)))

		:effect			(and (shakey-at	?s ?to)
						(not (shakey-at	?s ?from)))
	)

	;this action is needed to turn on the light in the room where Shakey is located
	;the room should also contain a box and should have switch.
	;Another precondition is that the light in the room initially should be turned off.
	(:action switch-light-on
		:parameters (?s - shakey ?b - box ?r - room)

		:precondition (and
						(shakey-at ?s ?r)
						(box-at ?b ?r)
						(has-switch ?r)
						(not (is-bright ?r)))

		:effect (is-bright ?r)
	)

	;this action is needed to turn off the light in the room where Shakey is located
	;same preconditions apply as in the previous action, except now the light in the room
	;should be turned on
	(:action switch-light-off
		:parameters (?s - shakey ?b - box ?r - room)

		:precondition (and
						(shakey-at ?s ?r)
						(box-at ?b ?r)
						(has-switch ?r)
						(is-bright ?r))

		:effect (not (is-bright ?r))
	)

	;Shakey can pick up object with this action
	;Both Shakey and the object should be in the same room
	;At least one of Shakey's grippers should be empty and the light should be turned on in the room
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

	;Shakey can put down the picked up object
	;To put down Shakey should be holding the object
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

	;Shakey can push around the box
	;Both Shakey and box should be in the room "from" initially
	;The door between two rooms should be wide to push box through it and the rooms should be adjacent
	;In the end both Shakey and box will end up in room "to"
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