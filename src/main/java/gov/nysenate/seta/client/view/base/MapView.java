package gov.nysenate.seta.client.view.base;

import com.google.common.collect.ImmutableMap;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

@XmlRootElement
public class MapView<KeyType, ViewType> implements ViewObject
{
    @XmlElement
    public final ImmutableMap<KeyType, ViewType> items;

    public static <KeyType, ViewType extends ViewObject> MapView<KeyType, ViewType> of(Map<KeyType, ViewType> items) {
        return new MapView<>(items);
    }
    public static <KeyType> MapView<KeyType, String> ofStringMap(Map<KeyType, String> items) {
        return new MapView<>(items);
    }
    public static <KeyType> MapView<KeyType, Integer> ofIntMap(Map<KeyType, Integer> items) {
        return new MapView<>(items);
    }

    private MapView(Map<KeyType, ViewType> map) {
        this.items = map != null ? ImmutableMap.copyOf(map) : ImmutableMap.of();
    }

    @XmlElement
    public int getSize() {
        return items.size();
    }

    @Override
    public String getViewType() {
        if (items.size()==0) {
            return "empty map";
        }
        String keyViewType = ViewObject.getViewTypeOf(items.keySet().iterator().next());
        String valueViewType = ViewObject.getViewTypeOf(items.values().iterator().next());
        return keyViewType + "," + valueViewType + " map";
    }
}
