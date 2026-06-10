using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace Library_Management_API.Migrations
{
    /// <inheritdoc />
    public partial class _08 : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.CreateIndex(
                name: "IX_FineTable_borrow_id",
                table: "FineTable",
                column: "borrow_id");

            migrationBuilder.AddForeignKey(
                name: "FK_FineTable_Borrow_RecordTable_borrow_id",
                table: "FineTable",
                column: "borrow_id",
                principalTable: "Borrow_RecordTable",
                principalColumn: "borrow_id",
                onDelete: ReferentialAction.Cascade);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_FineTable_Borrow_RecordTable_borrow_id",
                table: "FineTable");

            migrationBuilder.DropIndex(
                name: "IX_FineTable_borrow_id",
                table: "FineTable");
        }
    }
}
