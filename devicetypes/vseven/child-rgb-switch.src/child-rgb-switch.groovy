/**
 *  Child RGBLED Switch
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Change History:
 *
 *    Date        Who            What
 *    ----        ---            ----
 *    2017-10-01  Allan (vseven) Original Creation (based on Dan Ogorchock's child dimmer switch)
 * 
 */

// for the UI
metadata {
	definition (name: "Child RGB Switch", namespace: "vseven", author: "Alan (vseven) - based on code by Dan Ogorchock") {
	capability "Switch"
	capability "Color Control"

	command "generateEvent", ["string", "string"]
		
	attribute "lastColor", "string"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles (scale: 2){
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.illuminance.illuminance.bright", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.illuminance.illuminance.dark", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.illuminance.illuminance.light", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.illuminance.illuminance.light", backgroundColor:"#ffffff", nextState:"turningOn"
			}    
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
        			attributeState "level", action:"switch level.setLevel"
    			}
			tileAttribute ("device.color", key: "COLOR_CONTROL") {
				attributeState "color", action:"color control.setColor"
			}
		}
 		valueTile("lastUpdated", "device.lastUpdated", inactiveLabel: false, decoration: "flat", width: 6, height: 2) {
    			state "default", label:'Last Updated ${currentValue}', backgroundColor:"#ffffff"
		}

		main(["switch"])
		details(["switch", "level", "color", "lastUpdated"])
	}
}


void on() {
	def lastColor = device.latestValue("color.hex")
	log.debug("On pressed.  Turning status on and sending last known HEX value of $lastColor")
	//parent.childOn(device.deviceNetworkId)
	// Send the last hex value to turn back on
	//parent.childSetColor(device.deviceNetworkId, lastColor)
}

void off() {
	log.debug("Off pressed.  Sending HEX of #000000 but not updating device (retain last set color).")
	//parent.childOff(device.deviceNetworkId)
	// Send a all 0 hex value to turn off the LED
	//parent.childSetColor(device.deviceNetworkId, "#000000")
}

def setColor(value) {
    log.debug("Color value: $value")
    // Update our color and then just call the set level with the current level
    sendEvent(name: "color", value: value)
    def lastLevel = device.latestValue("level")
   log.debug("lastLevel: $lastLevel)
    if (lastLevel == null) {lastLevel = 100}
    setLevel(lastLevel)
}

def setLevel(level) {
    log.debug("Level value in percentage: $value")
    sendEvent(name: "level", value: level)
    //parent.childSetLevel(device.deviceNetworkId, level)
	
    // Turn on or off based on level selection
    if (level == 0) { off() } 
    else if (device.latestValue("switch") == "off") { on() }
    // Then if level is above 0 adjust the color
    if (level > 0) {
	// Get the last known color and if null use full on
	def colorHex = device.latestValue("color")
	if (colorHex == null) {colorHex = "#FFFFFF"}
	adjustColor(colorHex,level)
    }
}

def adjustColor(hex, level) {
    // Convert the hex color, apply the level, then send to the setColor routine
    def c = hexToRgb(colorHex)

    def r = hex(c.r * (level/100))
    def g = hex(c.g * (level/100))
    def b = hex(c.b * (level/100))

    def adjustedColor = "#" + $r + $g + $b
    log.debug("Adjusted color is $adjustedColor")
	
    //parent.childSetColorRGB(device.deviceNetworkId, adjustedColor)
}

def hexToRgb(colorHex) {
    def rrInt = Integer.parseInt(colorHex.substring(1,3),16)
    def ggInt = Integer.parseInt(colorHex.substring(3,5),16)
    def bbInt = Integer.parseInt(colorHex.substring(5,7),16)

    def colorData = [:]
    colorData = [r: rrInt, g: ggInt, b: bbInt]
    colorData
}

def generateEvent(String name, String value) {
  //log.debug("Passed values to routine generateEvent in device named $device: Name - $name  -  Value - $value")
  // The name coming in from ST_Anything will be "dimmerSwitch", but we want to the ST standard "switch" attribute for compatibility with normal SmartApps
  sendEvent(name: "switch", value: value)
  // Update lastUpdated date and time
  def nowDay = new Date().format("MMM dd", location.timeZone)
  def nowTime = new Date().format("h:mm a", location.timeZone)
  sendEvent(name: "lastUpdated", value: nowDay + " at " + nowTime, displayed: false)
}
