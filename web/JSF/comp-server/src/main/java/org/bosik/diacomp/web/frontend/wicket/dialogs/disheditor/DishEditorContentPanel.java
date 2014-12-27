package org.bosik.diacomp.web.frontend.wicket.dialogs.disheditor;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.bosik.diacomp.core.entities.business.dishbase.DishItem;
import org.bosik.diacomp.core.entities.tech.Versioned;
import org.bosik.diacomp.web.frontend.wicket.components.mealeditor.editor.MealEditor;
import org.bosik.diacomp.web.frontend.wicket.components.mealeditor.picker.food.FoodList;
import org.bosik.diacomp.web.frontend.wicket.dialogs.common.CommonEditorContentPanel;

public abstract class DishEditorContentPanel extends CommonEditorContentPanel<DishItem>
{
	private static final long	serialVersionUID	= 1L;

	public DishEditorContentPanel(String id, final IModel<Versioned<DishItem>> model)
	{
		super(id, model);
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();

		final Versioned<DishItem> modelObject = model.getObject();

		//dish.setData(new DishItem(dish.getData()));
		//FIXME

		form.add(new TextField<String>("inputName", new PropertyModel<String>(model, "data.name")));

		FoodList list = new FoodList();
		DishItem data = modelObject.getData();
		for (int i = 0; i < data.count(); i++)
		{
			list.getContent().add(data.get(i));
		}

		form.add(new MealEditor("editor", Model.of(list)));
	}
}
