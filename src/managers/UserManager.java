package managers;

import library.models.data.User;

public class UserManager {

	private static final UserManager instance = new UserManager();

	private User user = null;

	private UserManager() {

	}

	public static UserManager getInstance() {
		return instance;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}
