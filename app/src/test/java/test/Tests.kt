package test

import aleksey.vasiliev.bullfinchmail.model.general.Constants.PUBLIC_KEY
import org.junit.Test
import aleksey.vasiliev.bullfinchmail.model.general.DataBase
import aleksey.vasiliev.bullfinchmail.model.specific.ConversationLogic.messageTextIsCorrect
import aleksey.vasiliev.bullfinchmail.model.specific.ConversationLogic.transformTextForAMessage
import aleksey.vasiliev.bullfinchmail.model.specific.Message
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals

class Tests {

    @Test
    fun correctMessage() {
        assertTrue(messageTextIsCorrect("Замечательный мессенджер. Просто класс."))
        assertFalse(messageTextIsCorrect("a".repeat(2048)))
    }

    @Test
    fun transormedMessageText() {
        assertEquals("Lorem ipsum dolor sit amet, co\n" +
                "nsectetur adipiscing elit, sed\n" +
                " do eiusmod tempor incididunt \n" +
                "ut labore et dolore magna aliq\n" +
                "ua.", transformTextForAMessage("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."))
    }

    @Test
    fun keyStorageChecking() {
        val db = DataBase()
        assertEquals(db.createJSONKey(byteArrayOf(1, 12, 12, -100, 13), PUBLIC_KEY), "{\"publicKey\":[1,12,12,-100,13]}")
        assertNotEquals(db.createJSONKey(byteArrayOf(1, 12, 12, -100, 13), PUBLIC_KEY), "{\"privateKey\":[1,12,12,-100,13]}")
    }

    @Test
    fun messageCreatingChecking() {
        val db = DataBase()
        assertEquals(db.createMessageFromJSON(JSONObject("{\"date\":\"23.05.2020 12:14\",\"message\":\"Hi!\",\"gr\":1}")), Message("23.05.2020 12:14", "Hi!", 1))
    }
}