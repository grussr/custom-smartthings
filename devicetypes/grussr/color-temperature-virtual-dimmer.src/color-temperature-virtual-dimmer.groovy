metadata {
	definition (name: "Color Temperature Virtual Dimmer", namespace: "grussr", author: "Ryan Gruss") {
		capability "Actuator"
        capability "Color Temperature"
        capability "Switch Level"
        capability "Switch"
   	}

	// simulator metadata
	simulator {
	}

	// UI tile definitions
	tiles(scale: 2) {
    multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
    tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
        attributeState "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00a0dc", nextState:"turningOff"
        attributeState "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
        attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00a0dc", nextState:"turningOff"
        attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
    }
    tileAttribute ("device.level", key: "SLIDER_CONTROL") {
        attributeState "level", action:"switch level.setLevel"
    }
}
		controlTile("colorTempSliderControl", "device.colorTemperature", "slider", width: 4, height: 2, inactiveLabel: false, range: "(2700..6500)") {
			state "colorTemperature", action: "color temperature.setColorTemperature"
		}
		valueTile("colorTemp", "device.colorTemperature", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "colorTemperature", label: '${currentValue} K'
		}
		main "switch"
		details "switch", "colorTempSliderControl", "colorTemp"
	}
}

def installed() {
	log.debug "installed new dimmer switch"
}

def parse(String description) {
}

def setLevel(value) {
    log.debug "Setting level to ${value}"
    sendEvent(name: "level", value: value)
}

def on() {
	log.debug "turning on"
    sendEvent(name: "switch", value: "on")
    parent.turnOn(this)
}

def off() {
	log.debug "turning off"
    sendEvent(name: "switch", value: "off")
    parent.turnOff(this)
}

def setColorTemperature(value) {
	sendEvent(name: "colorTemperature", value: value)
}