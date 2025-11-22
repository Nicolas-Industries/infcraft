
package net.infcraft.core.world;

public interface IProgressUpdate {

    void displayProgressMessage(final String string);

    void displayLoadingString(final String string);

    void setLoadingProgress(final int integer);
}
