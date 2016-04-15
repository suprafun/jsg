## Java Scene Graph ##

Java Scene Graph is a simple and fast scene graph. It has a simple datastructure that is easy to use. It is build on LWJGL wich is a OpenGL binding for Java. Most of the important features of OpenGL are supported.

### Datastructure ###

The scene graph data structure looks like this:

SceneGraph ------ **RenderPass ------** Shape

The SceneGraph has a list of RenderPasses that is rendererd in order. The render pass can render to screen or a texture using pbo. The render pass has one or more Shapes that it renders. A Shape has a model matrix, state and vertex data. Shapes can be sorted on state, or by distance to the view.

Transform hierarchys are supported threw an optional TreeNode in RenderPass. All it does is add the shapes in the tree to the render pass and updates the shapes matrix when the tree treansorms change.

### Rendering ###

The renderer will optimise the rendering depending on usage. It detects when the the state, model matrix or vertex data don't change, and optimises accordingly. Static vertex data will be compiled to display lists. Static vertex data and static model matrix will compiled to display lists in world space. Shapes with static state will not be resorted every frame.

View-frustum culling is performed against the shapes bounding sphere.

The goal is to keep the renderer simple with as little overhead as possible.

### Status ###

Most of the features are implemented. The next step is to test, fix bugs and improve the api. I hope someone will have a look and provide some feedback.