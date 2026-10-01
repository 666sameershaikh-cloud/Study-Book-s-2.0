package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.UserProfile
import com.example.data.repository.ChatRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("World Books", appName)
  }

  @Test
  fun `deterministic chat id is consistent between users`() {
    val uid1 = "userA123"
    val uid2 = "userB456"

    val chatIdFromA = ChatRepository.getDeterministicChatId(uid1, uid2)
    val chatIdFromB = ChatRepository.getDeterministicChatId(uid2, uid1)

    assertEquals(chatIdFromA, chatIdFromB)
    assertEquals("userA123_userB456", chatIdFromA)
  }

  @Test
  fun `safe user profile from map handles null and missing fields without crashing`() {
    val incompleteMap = mapOf(
      "uid" to "test_user_789",
      "username" to "sameer_sk"
    )

    val profile = UserProfile.fromMap(incompleteMap, "test_user_789")
    assertNotNull(profile)
    assertEquals("test_user_789", profile.uid)
    assertEquals("sameer_sk", profile.username)
    assertEquals("sameer_sk", profile.usernameLowercase)
    assertEquals("offline", profile.onlineStatus)
  }
}
