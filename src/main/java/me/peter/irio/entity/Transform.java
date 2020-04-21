package me.peter.irio.entity;

import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Klass som håller positionen för spelaren och skalan den ska renderas i.
 */
public class Transform {
    public Vector3f pos;
    public Vector3f scale;

    /**
     * Skapar ett nytt objekt av klassen Transform.
     */
    public Transform(){
        pos = new Vector3f();
        scale = new Vector3f();
    }

    /**
     * Skalar upp och sätter korrekt position. "Översätter"
     * @param target Projektionen vi ska ändra på
     * @return Ny projektion för GPU:n att använda.
     */
    public Matrix4f getProjection(Matrix4f target) {
        target.scale(scale);
        target.translate(pos);
        return target;
    }
}
