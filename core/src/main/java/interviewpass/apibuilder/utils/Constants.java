package interviewpass.apibuilder.utils;

public class Constants {

    public static final String COMMA = ",";
    public static final String DOT = ".";

    public static final String RESPONSE_JSON_SETTING = "application/json; charset=UTF-8";

    public static final String USER_ID_PARAMETER = "userID";
    public static final String AUTH_TYPE_PARAMETER = "authType";

    public static final String STATUS_REGISTRATION_SUCCESS = "Новый аккаунт был создан.";

    public static final String STATUS_USER_DOESNOT_EXIST = "user with that name doesn't exist";

    public static final String STATUS_USER_IS_INVALID = "please see you mail and follow to the instructions";

    public static final String STATUS_WRONG_PASS = "wrong password";

    public static final String STATUS_SUCCESS_LOGIN = "successLogin";

    public static final String STATUS_REGISTRATION_FAIL = "Что-то пошло не так. Попробуйте загерестрироваться позже или упритесь в стену.";

    public static final String AUTH_COOKIE_NAME = "api-builder-session-id";

    public static final String EMAIL_COOKIE_NAME = "api-builder-user";

    public static final int LOGIN_COOKIE_AGE = 60 * 60 * 24 * 60;

    public static final String CATALOG_ROOT_PAGE_PATH = "/content/api-builder/catalog";

    static final String USER_QUERY = "SELECT * FROM [nt:base] AS user WHERE ISDESCENDANTNODE([/home/users/api-builder/users]) AND NAME() = 'profile' AND user.[userId] = '%s'";
}
