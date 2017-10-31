package org.egordorichev.lasttry.entity;

import org.egordorichev.lasttry.entity.component.Component;
import org.egordorichev.lasttry.util.log.Log;

import java.util.HashMap;

/**
 * The base class for in-game entities
 */
public class Entity {
	/**
	 * Registered components
	 */
	protected HashMap<Class<? extends Component>, Component> components;

	public Entity() {
		this.components = new HashMap<>();
	}

	/**
	 * Updates the entity
	 *
	 * @param delta Time since the last frame
	 */
	public void update(float delta) {

	}

	/**
	 * Renders the entity
	 */
	public void render() {

	}

	/**
	 * Registers a component
	 *
	 * @param component Component class to register
	 */
	public void addComponent(Class<? extends Component> component) {
		Component instance = null;
		try {
			instance = component.newInstance();
		} catch (Exception exception) {
			Log.error("Failed to create a component " + component.getName());
		}

		this.components.put(component, instance);
	}

	/**
	 * Removes a component
	 *
	 * @param component Component class to remove
	 */
	public void deleteComponent(Class<? extends Component> component) {
		this.components.remove(component);
	}

	/**
	 * Searches for a component
	 *
	 * @param component Component class to find
	 * @return Entity has a component
	 */
	public boolean hasComponent(Class<? extends Component> component) {
		return this.components.containsKey(component);
	}

	/**
	 * Searches for a component and returns it
	 *
	 * @param component Component class to find
	 * @return Component or null
	 */
	public Component getComponent(Class<? extends Component> component) {
		return this.components.get(component);
	}
}