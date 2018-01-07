package manuelbarzi;

import java.io.Serializable;

/**
 * Dummy bean interface.
 * 
 * @author manuelbarzi
 */
public interface IMyBean extends Serializable {

	String getData();

	void setData(String data);

}