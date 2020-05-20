package aleksey.vasiliev.bullfinchmail.model.general

import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.makeByteArray

object ProtocolPhrases {

    val SIGN_UP_RESPONSE = "I want to sign up.".makeByteArray()
    val EXCHANGE_KEYS_RESPONSE = "I want to exchange keys.".makeByteArray()
    val STOP_IT_RESPONSE = "Stop it.".makeByteArray()
    val SEND_MESSAGE_RESPONSE = "I want to send a message.".makeByteArray()
    val SUCCEED_RESPONSE = "Succeed.".makeByteArray()
    val AMOUNT_RECEIVED_RESPONSE = "Amount received.".makeByteArray()
    val CHECK_UPDATES_RESPONSE = "I want to check for friends requests and new messages.".makeByteArray()
    val MAKE_FRIENDS_RESPONSE = "I want to make friends.".makeByteArray()
    val CHANGE_USERNAME_RESPONSE = "I want to change a username.".makeByteArray()

    const val CORRECT_LOGIN_COMMAND = "Login is correct."
    const val CORRECT_PASSWORD_COMMAND = "Password is correct."
    const val SUCCESS_COMMAND = "Success!"
    const val VALID_USER_COMMAND = "I know this user."

    const val REGISTRATION_WARNING_PHRASE = "Either login is already in use, or connection was unavailable. Try again."
    const val REGISTRATION_SUCCESS_PHRASE = "You were signed up! Congratulations!"
    const val REQUEST_SENT_PHRASE = "You sent a friend request."
    const val REQUEST_WARNING_PHRASE = "Either user doesn't exist, or connection is unavailable. Try again."
    const val MESSAGE_NOT_SENT_PHRASE = "Due to unknown errors, message wasn't sent."
    const val LOGIN_AND_PASSWORD_WARNING_PHRASE = "Login, or password has incorrect format. Use only letters, digits, -._. Login should contain at least three letters, password - 8."
    const val INCORRECT_USERNAME_PHRASE = "Username has incorrect format. Use only letters, digits, -._. It should contain at least three letters."
    const val ACCEPT_PHRASE = "Accept"
    const val NEW_USERNAME_PHRASE = "Input new username."
    const val UPDATE_PHRASE = "You have a new message or friend request."
}