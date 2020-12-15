/**
 * Holds processors that can be used by the extension. Processors are responsible for changing the contents of the message
 * before they are changed.
 * Note that they are unused in some cases like when a player joins or quits and they are only able to change what the message
 * contents are, that is what later is used as the message, that the player typed. it is called before the formatter, so
 * formatting should not be done via processors.
 */
package org.kitteh.craftirc.messaging.processing;
