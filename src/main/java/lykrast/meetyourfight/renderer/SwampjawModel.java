package lykrast.meetyourfight.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import lykrast.meetyourfight.entity.SwampjawEntity;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

// Made with Blockbench 3.7.4
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
public class SwampjawModel extends EntityModel<SwampjawEntity> {
	private final ModelRenderer bodyMain;
	private final ModelRenderer finRight;
	private final ModelRenderer finLeft;
	private final ModelRenderer tailfinBottom;
	private final ModelRenderer tailfinTop;
	private final ModelRenderer tailOuter;
	private final ModelRenderer tailInner;
	private final ModelRenderer jaw;
	private final ModelRenderer head;
	
	//Blockbench constant
	private static final float TAILFIN_PITCH = 0.2618F;
	
	private float tailYaw, tailPitch;

	public SwampjawModel() {
		texWidth = 128;
		texHeight = 64;
		
		//Blockbench whyyyyy you grouping stuff
		//Had to rotate all the things I wanted separate by a little bit so that I got a second export where they're separate
		bodyMain = new ModelRenderer(this);
		bodyMain.setPos(0.0F, 24.0F, 0.0F);
		bodyMain.texOffs(40, 0).addBox(-6.0F, -10.0F, -6.0F, 12.0F, 10.0F, 12.0F, 0.0F, false);

		finRight = new ModelRenderer(this);
		finRight.setPos(-6.0F, -5.0F, 0.0F);
		bodyMain.addChild(finRight);
		setRotationAngle(finRight, 0.0F, 0.0F, -0.4363F);
		finRight.texOffs(0, 28).addBox(-8.0F, 0.0F, -2.0F, 8.0F, 1.0F, 4.0F, 0.0F, true);

		finLeft = new ModelRenderer(this);
		finLeft.setPos(6.0F, -5.0F, 0.0F);
		bodyMain.addChild(finLeft);
		setRotationAngle(finLeft, 0.0F, 0.0F, 0.4363F);
		finLeft.texOffs(0, 28).addBox(0.0F, 0.0F, -2.0F, 8.0F, 1.0F, 4.0F, 0.0F, false);

		tailInner = new ModelRenderer(this);
		tailInner.setPos(0.0F, -10.0F, 6.0F);
		bodyMain.addChild(tailInner);
		tailInner.texOffs(40, 22).addBox(-5.0F, 0.0F, 0.0F, 10.0F, 8.0F, 8.0F, 0.0F, false);

		tailOuter = new ModelRenderer(this);
		tailOuter.setPos(0, 0, 8);
		tailInner.addChild(tailOuter);
		tailOuter.texOffs(40, 38).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 2.0F, 6.0F, 0.0F, false);

		tailfinTop = new ModelRenderer(this);
		tailfinTop.setPos(0, 1, 5);
		tailOuter.addChild(tailfinTop);
		setRotationAngle(tailfinTop, -TAILFIN_PITCH, 0.0F, 0.0F);
		tailfinTop.texOffs(0, 33).addBox(-0.5F, -10.0F, 0.0F, 1.0F, 10.0F, 5.0F, 0.0F, false);

		tailfinBottom = new ModelRenderer(this);
		tailfinBottom.setPos(0, 1, 5);
		tailOuter.addChild(tailfinBottom);
		setRotationAngle(tailfinBottom, TAILFIN_PITCH, 0.0F, 0.0F);
		tailfinBottom.texOffs(12, 33).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 10.0F, 5.0F, -0.1F, false);

		head = new ModelRenderer(this);
		head.setPos(0.0F, -5.0F, -6.0F);
		bodyMain.addChild(head);
		head.texOffs(0, 0).addBox(-5.0F, -4.0F, -10.0F, 10.0F, 6.0F, 10.0F, 0.0F, false);

		jaw = new ModelRenderer(this);
		jaw.setPos(0, 2, 0);
		head.addChild(jaw);
		jaw.texOffs(0, 16).addBox(-5.0F, 0.0F, -10.0F, 10.0F, 2.0F, 10.0F, 0.0F, false);
	}

	@Override
	public void prepareMobModel(SwampjawEntity entity, float limbSwing, float limbSwingAmount, float partialTick) {
		super.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);
		tailYaw = MathHelper.degreesDifference(entity.yRot, entity.getTailYaw(partialTick)) / 3F;
		tailPitch = -MathHelper.degreesDifference(entity.xRot, entity.getTailPitch(partialTick)) / 1.5F;
		tailYaw *= (float)Math.PI / 180F;
		tailPitch *= (float)Math.PI / 180F;
	}

	@Override
	public void setupAnim(SwampjawEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		//bodyMain.rotateAngleX = entity.rotationPitch * ((float) Math.PI / 180F);
		
		//Head
		head.xRot = headPitch * ((float) Math.PI / 180F);
		head.yRot = netHeadYaw * ((float) Math.PI / 180F);
		
		//Flap fins
		finLeft.yRot = MathHelper.cos(limbSwing * 0.2F) * 0.8F * limbSwingAmount;
		finRight.yRot = MathHelper.cos(limbSwing * 0.2F + (float)Math.PI) * 0.8F * limbSwingAmount;
		
		//Rotate tail
		tailInner.xRot = tailPitch;
		tailInner.yRot = tailYaw + MathHelper.cos(limbSwing * 0.35F) * 0.15F * limbSwingAmount;
		tailOuter.xRot = tailPitch;
		tailOuter.yRot = tailYaw + MathHelper.cos(limbSwing * 0.35F + (float)Math.PI / 3F) * 0.15F * limbSwingAmount;
		tailfinTop.yRot = tailYaw + MathHelper.cos(limbSwing * 0.35F + 2F * (float)Math.PI / 3F) * 0.15F * limbSwingAmount;
		tailfinBottom.yRot = tailfinTop.yRot;
	}

	@Override
	public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha){
		bodyMain.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.xRot = x;
		modelRenderer.yRot = y;
		modelRenderer.zRot = z;
	}
}