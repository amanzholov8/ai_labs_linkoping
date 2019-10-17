(define (problem lights-on-in-all-rooms)
  	(:domain shakey-world)

  	(:objects 
  			s - Shakey
  			r1 r2 r3 - room
  			b - box
  			g1 g2 - gripper)
  	
  	;initial conditions taken from picture in the lab description page
  	(:init
  			(shakey-at s r2)
  			(box-at b r1)
  			(has-switch r1) (has-switch r2) (has-switch r3)
  			(adjacent r1 r2) (adjacent r2 r3)
  			(wide-entrance r1 r2) (wide-entrance r2 r3)
  	)

  	(:goal (and (is-bright r1) (is-bright r2) (is-bright r3)))
)