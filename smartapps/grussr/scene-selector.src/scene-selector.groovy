/**
 *  Scene Selector
 *
 *  Copyright 2019 Ryan Gruss
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
 */
definition(
    name: "Scene Selector",
    namespace: "grussr",
    author: "Ryan Gruss",
    description: "Set a list of scenes to choose from by pressing a button",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png") {
    appSetting "apikey"
}


preferences {
	page name: "mainPage", title: "Select scenes and trigger button", install: false, uninstall: true, nextPage: "namePage"
    page name: "namePage", title: "Name", install: true, uninstall: true
}

def mainPage() {
	dynamicPage(name: "mainPage"){
    	section("When this button is pressed...") {
        	input name: "triggerButton", type: "capability.button", title: "Select Button", required: true, multiple: false
        }
		section("Choose your scenes") {
            def sceneList = getScenes()
            input name: "selectedScene1", type: "enum", title: "Scene #1", required: true, multiple: false, options: sceneList
            input name: "selectedScene2", type: "enum", title: "Scene #2", required: false, multiple: false, options: sceneList
            input name: "selectedScene3", type: "enum", title: "Scene #3", required: false, multiple: false, options: sceneList
            input name: "selectedScene4", type: "enum", title: "Scene #4", required: false, multiple: false, options: sceneList
 }
	}
}

def namePage() {
    if (!overrideLabel) {
        // if the user selects to not change the label, give a default label
        def l = defaultLabel()
        log.debug "will set default label of $l"
        app.updateLabel(l)
    }
    dynamicPage(name: "namePage") {
        if (overrideLabel) {
            section("Automation name") {
                label title: "Enter custom name", defaultValue: app.label, required: false
            }
        } else {
            section("Automation name") {
                paragraph app.label
            }
        }
        section {
            input "overrideLabel", "bool", title: "Edit automation name", defaultValue: "false", required: "false", submitOnChange: true
        }
    }
}

def defaultLabel() {
    //def bulbLabel = cLights.size() == 1 ? cLights[0].displayName : cLights[0].displayName + ", etc..."
    "Selectascene"
}

def getScenes() {
	def result = []
    def sceneIdMap = [:]
    def params = [
        uri: "https://api.smartthings.com/v1/scenes",
        headers: [ Authorization: "Bearer " + appSettings.apikey ]
    ]

    try {
        httpGet(params) { resp ->
        log.debug "response data: ${resp.data}"
        resp.data.items.each {
        	result << it.sceneName
            sceneIdMap[it.sceneName] = it.sceneId
        }
        }
    } catch (e) {
        log.error "something went wrong: $e"
    }
    state.idMap = sceneIdMap
    return result.sort()
}

def getSceneId(sceneName) {
	return state.idMap[sceneName]
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
    initialize()
}

def initialize() {
    log.debug state
	state.lastscene = 0
    state.activeScenes = [1: getSceneId(selectedScene1)]
    log.debug state
    subscribe(triggerButton, "button.pushed", onPush())
	
}

def onPush() {
	log.debug "button pushed"
    
}