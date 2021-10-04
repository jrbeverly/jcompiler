package ca.uwaterloo.joos1wc.scanner;

import java.util.UUID;

/**
 * Pairs a token with a unique identifier.
 */
public class TokenEntry {

	private final UUID uid;
	private final Token token;

	/**
	 * Constructs a token with a unique identifier.
	 * 
	 * @param id
	 *            Unique identifier for the specific token.
	 * @param token
	 *            A lexical unit.
	 */
	public TokenEntry(UUID id, Token token) {
		this.uid = id;
		this.token = token;
	}

	/**
	 * Gets the unique identifier of the token.
	 * 
	 * @return Unique identifier of the token.
	 */
	public UUID getUUID() {
		return uid;
	}

	/**
	 * Gets the token.
	 * 
	 * @return A token.
	 */
	public Token getToken() {
		return token;
	}
}
