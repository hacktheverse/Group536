import UIKit
import Starscream
import JLToast

class ViewController: UIViewController, WebSocketDelegate {

	@IBOutlet weak var gyroscopeLabel: UILabel!
	@IBOutlet weak var status: UILabel!
	@IBOutlet weak var accelLabel: UILabel!
	
	var currentZ: Float = 0.0
	var lastZ: Float = 0.0
	var theMax: Float = 0.0
	var theMin: Float = 0.0
	var message = ""
	var controller: UINavigationController? = nil

	var socket = WebSocket(url: NSURL(string: "ws://f732a04e.ngrok.io")!)

	override func viewDidLoad() {
		super.viewDidLoad()
		socket.delegate = self
		socket.connect()

		NSNotificationCenter.defaultCenter().addObserver(self, selector: "didRecieveGyroScopeEvent:", name: TLMMyoDidReceiveGyroscopeEventNotification, object: nil)
		NSNotificationCenter.defaultCenter().addObserver(self, selector: "didReceiveAccelEvent:", name: TLMMyoDidReceiveAccelerometerEventNotification, object: nil)

		JLToastView.setDefaultValue(
			UIColor.redColor(),
			forAttributeName: JLToastViewBackgroundColorAttributeName,
			userInterfaceIdiom: .Phone
		)
	}

	// MARK: - Myo stuff

	@IBAction func didTapSettings(sender: AnyObject) {
		// Settings view must be in a navigation controller when presented
		controller = TLMSettingsViewController.settingsInNavigationController()
		presentViewController(controller!, animated: true, completion: nil)
	}

	func didRecieveGyroScopeEvent(notification: NSNotification) {
		let eventData = notification.userInfo as! Dictionary<NSString, TLMGyroscopeEvent>
		let gyroEvent = eventData[kTLMKeyGyroscopeEvent]!

		let date = NSDate().timeIntervalSince1970

		let gyroData = GLKitPolyfill.getGyro(gyroEvent)

		currentZ = gyroData.z
		lastZ = lastZ == 0 ? currentZ : lastZ

		theMax = max(currentZ, theMax)
		theMin = min(currentZ, theMin)

		if lastZ < 0 && currentZ > 0 {
//			print("ping")

			let dataString = "{ \"timestamp\": \(date), \"peak\": \(theMax), \"trough\": \(theMin) }"
			NSLog("\(theMax), \(theMin)")

			socket.writeString(dataString)

			theMax = 0
			theMin = 0
		}

		gyroscopeLabel.text = "Gyro: \(currentZ)"
		lastZ = currentZ

	}

	func didReceiveAccelEvent(notification: NSNotification) {
		let eventData = notification.userInfo as! Dictionary<NSString, TLMAccelerometerEvent>
		let data = eventData[kTLMKeyAccelerometerEvent]
		if let vec = data?.vector {
			let result = TLMVector3Length(vec)
			self.accelLabel.text = "Accel: \(result)"
		}
	}





	// MARK: - websocket stuff

	func websocketDidConnect(ws: WebSocket) {
		NSLog("websocket is connected")
		JLToast.makeText("server connected").show()

	}

	func websocketDidDisconnect(ws: WebSocket, error: NSError?) {
		if let e = error {
			NSLog("websocket is disconnected: \(e.localizedDescription)")
		} else {
			NSLog("websocket disconnected")
		}
		JLToast.makeText("server disconnected").show()

		self.controller?.dismissViewControllerAnimated(true, completion: nil)

	}

	func websocketDidReceiveMessage(ws: WebSocket, text: String) {
		if message != text {
			NSLog("Updated gait: \(text)")
		}
		message = text
		let regex = "(sprinting|running|walking|standing|unknown)"
		let re = try! NSRegularExpression(pattern: regex, options: [])

		let result = re.firstMatchInString(text as String, options: [], range: NSMakeRange(0, (text as NSString).length))

		if let result = result {
			self.status.text = (text as NSString).substringWithRange(result.rangeAtIndex(1))
		}

	}

	func websocketDidReceiveData(ws: WebSocket, data: NSData) {
		NSLog("Received data: \(data.length)")
	}

	@IBAction func toConnectServer(sender: UIButton) {
		socket.connect()
	}
	@IBAction func toDisconnectServer(sender: UIButton) {
		socket.disconnect()
	}
}

