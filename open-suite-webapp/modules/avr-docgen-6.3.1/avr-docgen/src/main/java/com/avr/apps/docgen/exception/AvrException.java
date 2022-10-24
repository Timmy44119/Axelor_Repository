package com.avr.apps.docgen.exception;

import com.axelor.db.Model;
import com.axelor.exception.AxelorException;

import java.util.List;

public class AvrException extends AxelorException {

    public final static String PROGRAMMING_LINE_MISSING = "Les lignes de programmation ne doivent pas être vide";
    public final static String TARGET_MISSING = "Aucune cible selectionné";
    public final static String NO_MOVIE_ACTIF = "Aucun film n'est disponible.\n Soit aucun film n'a été ajouté soit aucun film n'est activé.";
    public final static String NO_STATUS_LINE_EXISTE = "Le status ligne avec le code %s n'existe pas. Merci de le créer.";
    public final static String FTP_CONNECT_FAILED = "La connection au ftp à échouer";
    public final static String FTP_LOGIN_FAILED = "Les identifiant au ftp sont incorrect";
    public final static String NO_PLANNING_FOUND = "Le planning pour l'employee %s n'a pas été trouvé";

    /**
     * Create an exception with a category and a message.
     *
     * @param category
     * @param message
     * @param messageArgs
     */
    public AvrException(int category, String message, Object... messageArgs) {
        super(category, message, messageArgs);
    }


    public AvrException(int category, List<String> messages, Object... messageArgs) {
        super(category, String.join("<br>\n", messages), messageArgs);
    }

    /**
     * Create an exception with his cause and his type.
     *
     * @param cause The exception cause
     * @param category
     *     <ul>
     *       <li>1: Missing field
     *       <li>2: No unique key
     *       <li>3: No value
     *       <li>4: configuration error
     *       <li>5: CATEGORY_INCONSISTENCY
     *     </ul>
     *
     * @see Throwable
     */
    public AvrException(Throwable cause, int category) {
        super(cause,category);
    }

    /**
     * Create an exception with a cause, a category, and a message.
     *
     * @param cause
     * @param category
     * @param message
     * @param messageArgs
     */
    public AvrException(Throwable cause, int category, String message, Object... messageArgs) {
        super(cause, category, message, messageArgs);
    }

    /**
     * Create an exception with a reference class, a category, and a message.
     *
     * @param refClass
     * @param category
     * @param message
     * @param messageArgs
     */
    public AvrException(
            Class<? extends Model> refClass, int category, String message, Object... messageArgs) {
        super(refClass, category, message, messageArgs);
    }

    /**
     * Create an exception with a cause, a reference class, and a category.
     *
     * @param cause
     * @param refClass
     * @param category
     */
    public AvrException(Throwable cause, Class<? extends Model> refClass, int category) {
        super(cause, refClass, category);
    }

    /**
     * Create an exception with a cause, a reference class, a category, and a message.
     *
     * @param cause
     * @param refClass
     * @param category
     * @param message
     * @param messageArgs
     */
    public AvrException(
            Throwable cause,
            Class<? extends Model> refClass,
            int category,
            String message,
            Object... messageArgs) {
        super(cause, refClass, category, message, messageArgs);
    }

    /**
     * Create an exception with a reference, a category, and a message.
     *
     * @param ref
     * @param category
     * @param message
     * @param messageArgs
     */
    public AvrException(Model ref, int category, String message, Object... messageArgs) {
        super(ref, category, message,messageArgs);
    }

    /**
     * Create an exception with a reference, a category, and a message.
     *
     * @param ref
     * @param category
     * @param messages
     * @param messageArgs
     */
    public AvrException(Class<? extends Model> ref, int category, List<String> messages, Object... messageArgs) {
        super(ref, category,  String.join("<br>\n", messages), messageArgs);
    }

    /**
     * Create an exception with a reference, a category, and a message.
     *
     * @param ref
     * @param category
     * @param messages
     */
    public AvrException(Class<? extends Model> ref, int category, List<String> messages) {
        super(ref, category,  String.join("<br>\n", messages));
    }

    /**
     * Create an exception with a cause, a reference, and a category.
     *
     * @param cause
     * @param ref
     * @param category
     */
    public AvrException(Throwable cause, Model ref, int category) {
        super(cause, ref,category);
    }

    /**
     * Create an exception with a cause, a reference, a category, and a message.
     *
     * @param cause
     * @param ref
     * @param category
     * @param message
     * @param messageArgs
     */
    public AvrException(
            Throwable cause, Model ref, int category, String message, Object... messageArgs) {
        super(ref, category,message,messageArgs);
    }

}
