import unittest

from sensory_calibration import calibrate_signal


class TestCalibrateSignal(unittest.TestCase):
    def test_default(self):
        self.assertAlmostEqual(calibrate_signal(), 1.1)

    def test_custom_baseline(self):
        self.assertAlmostEqual(calibrate_signal(2.0), 2.2)

    def test_zero_baseline(self):
        self.assertAlmostEqual(calibrate_signal(0.0), 0.0)


if __name__ == "__main__":
    unittest.main()
