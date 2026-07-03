package com.example.flaggameandroid.feature.app

import junit.framework.TestCase

fun assertEquals(expected: Int, actual: Double) {
  TestCase.assertEquals(expected.toDouble(), actual)
}

fun assertEquals(expected: Int, actual: Double, delta: Double) {
  TestCase.assertEquals(expected.toDouble(), actual, delta)
}

