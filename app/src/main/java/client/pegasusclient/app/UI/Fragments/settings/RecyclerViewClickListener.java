package client.pegasusclient.app.UI.Fragments.settings;


/**
 * @author  Tamir Sagi
 * this interface is setting cecycler list to detect when item is clicked
 */
public interface RecyclerViewClickListener {

    /**
     * get the position of clicked item
     * @param position
     */
    void getClickedItemPosition(int position);



}
