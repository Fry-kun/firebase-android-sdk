// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.firebase.functions

import android.util.SparseArray
import com.google.firebase.FirebaseException
import org.json.JSONException
import org.json.JSONObject

// TODO: This is a copy of FirebaseFirestoreException.
// We should investigate whether we can at least share the Code enum.
/** The class for all Exceptions thrown by FirebaseFunctions. */
public class FirebaseFunctionsException : FirebaseException {
  /**
   * The set of error status codes that can be returned from a Callable HTTPS tigger. These are the
   * canonical error codes for Google APIs, as documented here:
   * https://github.com/googleapis/googleapis/blob/master/google/rpc/code.proto#L26
   */
  public enum class Code(@Suppress("unused") private val value: Int) {
    /**
     * The operation completed successfully. `FirebaseFunctionsException` will never have a status
     * of `OK`.
     */
    OK(0),

    /** The operation was cancelled (typically by the caller). */
    CANCELLED(1),

    /** Unknown error or an error from a different error domain. */
    UNKNOWN(2),

    /**
     * Client specified an invalid argument. Note that this differs from `FAILED_PRECONDITION`.
     * `INVALID_ARGUMENT` indicates arguments that are problematic regardless of the state of the
     * system (For example, an invalid field name).
     */
    INVALID_ARGUMENT(3),

    /**
     * Deadline expired before operation could complete. For operations that change the state of the
     * system, this error may be returned even if the operation has completed successfully. For
     * example, a successful response from a server could have been delayed long enough for the
     * deadline to expire.
     */
    DEADLINE_EXCEEDED(4),

    /** Some requested document was not found. */
    NOT_FOUND(5),

    /** Some document that we attempted to create already exists. */
    ALREADY_EXISTS(6),

    /** The caller does not have permission to execute the specified operation. */
    PERMISSION_DENIED(7),

    /**
     * Some resource has been exhausted, perhaps a per-user quota, or perhaps the entire file system
     * is out of space.
     */
    RESOURCE_EXHAUSTED(8),

    /**
     * Operation was rejected because the system is not in a state required for the operation's
     * execution.
     */
    FAILED_PRECONDITION(9),

    /**
     * The operation was aborted, typically due to a concurrency issue like transaction aborts, etc.
     */
    ABORTED(10),

    /** Operation was attempted past the valid range. */
    OUT_OF_RANGE(11),

    /** Operation is not implemented or not supported/enabled. */
    UNIMPLEMENTED(12),

    /**
     * Internal errors. Means some invariants expected by underlying system has been broken. If you
     * see one of these errors, something is very broken.
     */
    INTERNAL(13),

    /**
     * The service is currently unavailable. This is a most likely a transient condition and may be
     * corrected by retrying with a backoff.
     */
    UNAVAILABLE(14),

    /** Unrecoverable data loss or corruption. */
    DATA_LOSS(15),

    /** The request does not have valid authentication credentials for the operation. */
    UNAUTHENTICATED(16);

    public companion object {
      // Create the canonical list of Status instances indexed by their code values.
      private val STATUS_LIST = buildStatusList()
      private fun buildStatusList(): SparseArray<Code> {
        val codes = SparseArray<Code>()
        for (c in values()) {
          val existingValue = codes[c.ordinal]
          check(existingValue == null) {
            "Code value duplication between " + existingValue + "&" + c.name
          }
          codes.put(c.ordinal, c)
        }
        return codes
      }

      @JvmStatic
      public fun fromValue(value: Int): Code {
        return STATUS_LIST[value, UNKNOWN]
      }

      /**
       * Takes an HTTP status code and returns the corresponding [Code] error code. This is the
       * standard HTTP status code -> error mapping defined in:
       * https://github.com/googleapis/googleapis/blob/master/google/rpc/code.proto
       *
       * @param status An HTTP status code.
       * @return The corresponding `Code`, or `Code.UNKNOWN` if none.
       */
      @JvmStatic
      public fun fromHttpStatus(status: Int): Code {
        when (status) {
          200 -> return OK
          400 -> return INVALID_ARGUMENT
          401 -> return UNAUTHENTICATED
          403 -> return PERMISSION_DENIED
          404 -> return NOT_FOUND
          409 -> return ABORTED
          429 -> return RESOURCE_EXHAUSTED
          499 -> return CANCELLED
          500 -> return INTERNAL
          501 -> return UNIMPLEMENTED
          503 -> return UNAVAILABLE
          504 -> return DEADLINE_EXCEEDED
        }
        return UNKNOWN
      }
    }
  }

  /**
   * Gets the error code for the operation that failed.
   *
   * @return the code for the `FirebaseFunctionsException`
   */
  public val code: Code

  /**
   * Gets the details object, if one was included in the error response.
   *
   * @return the object included in the "details" field of the response.
   */
  public val details: Any?

  internal constructor(message: String, code: Code, details: Any?) : super(message) {
    this.code = code
    this.details = details
  }

  internal constructor(
    message: String,
    code: Code,
    details: Any?,
    cause: Throwable?
  ) : super(message, cause!!) {
    this.code = code
    this.details = details
  }

  internal companion object {
    /**
     * Takes an HTTP response and returns the corresponding Exception if any.
     *
     * @param code An HTTP status code.
     * @param body The body of the HTTP response, if any.
     * @param serializer A serializer to use for decoding error details.
     * @return The corresponding Exception, or null if none.
     */
    @JvmStatic
    internal fun fromResponse(
      code: Code,
      body: String?,
      serializer: Serializer
    ): FirebaseFunctionsException? {
      // Start with reasonable defaults from the status code.
      var actualCode = code
      var description = actualCode.name
      var details: Any? = null

      // Then look through the body for explicit details.
      try {
        val json = JSONObject(body ?: "")
        val error = json.getJSONObject("error")
        if (error.opt("status") is String) {
          actualCode = Code.valueOf(error.getString("status"))
          // TODO: Add better default descriptions for error enums.
          // The default description needs to be updated for the new code.
          description = actualCode.name
        }
        if (error.opt("message") is String && error.getString("message").isNotEmpty()) {
          description = error.getString("message")
        }
        details = error.opt("details")
        if (details != null) {
          details = serializer.decode(details)
        }
      } catch (iae: IllegalArgumentException) {
        // This most likely means the status string was invalid, so consider this malformed.
        actualCode = Code.INTERNAL
        description = actualCode.name
      } catch (ioe: JSONException) {
        // If we couldn't parse explicit error data, that's fine.
      }
      return if (actualCode == Code.OK) {
        // Technically, there's an edge case where a developer could explicitly return an error code
        // of OK, and we will treat it as success, but that seems reasonable.
        null
      } else FirebaseFunctionsException(description, actualCode, details)
    }
  }
}
