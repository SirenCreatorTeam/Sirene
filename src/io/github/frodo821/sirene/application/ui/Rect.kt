package io.github.frodo821.sirene.application.ui

class Rect(x: Double, y: Double, w: Double, h: Double)
{
	val x = x
	val y = y
	val w = w
	val h = h
	var minX = x
	get ()
	{
		return if(w <= 0) x + w else x
	}
	var minY = y
	get ()
	{
		return if(h <= 0) y + h else y
	}
	var maxX = x
	get ()
	{
		return if(w >= 0) x + w else x
	}
	var maxY = y
	get ()
	{
		return if(h >= 0) y + h else y
	}
	
	
	fun isIncluding(ox: Double, oy: Double): Boolean
	{
		return ox >= minX && ox <= maxX && oy >= minY && oy <= maxY
	}
}