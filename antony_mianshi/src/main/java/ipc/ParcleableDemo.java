package ipc;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcleableDemo  implements Parcelable {

    private String address;
    private int age;
    private String name;

    private ParcleableDemo(Parcel in) {
        address = in.readString();
        age = in.readInt();
        name = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        dest.writeInt(age);
        dest.writeString(name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ParcleableDemo> CREATOR = new Creator<ParcleableDemo>() {
        @Override
        public ParcleableDemo createFromParcel(Parcel in) {
            return new ParcleableDemo(in);
        }

        @Override
        public ParcleableDemo[] newArray(int size) {
            return new ParcleableDemo[size];
        }
    };
}
